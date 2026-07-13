/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.sync

import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.contact.ContactIdInternal
import ch.abwesend.privatecontacts.domain.model.contact.IContactIdInternal
import ch.abwesend.privatecontacts.domain.model.sync.ConflictKind
import ch.abwesend.privatecontacts.domain.model.sync.ContactSyncState
import ch.abwesend.privatecontacts.domain.model.sync.CreateContactCommand
import ch.abwesend.privatecontacts.domain.model.sync.DeleteContactCommand
import ch.abwesend.privatecontacts.domain.model.sync.SyncCommandEnvelope
import ch.abwesend.privatecontacts.domain.model.sync.SyncCommandOperation
import ch.abwesend.privatecontacts.domain.model.sync.SyncConflict
import ch.abwesend.privatecontacts.domain.model.sync.SyncContact
import ch.abwesend.privatecontacts.domain.model.sync.SyncOutboxCommand
import ch.abwesend.privatecontacts.domain.model.sync.SyncSnapshot
import ch.abwesend.privatecontacts.domain.model.sync.SyncSnapshotContact
import ch.abwesend.privatecontacts.domain.model.sync.UpdateContactCommand
import ch.abwesend.privatecontacts.domain.repository.IContactRepository
import ch.abwesend.privatecontacts.domain.repository.ISyncConflictRepository
import ch.abwesend.privatecontacts.domain.repository.ISyncOutboxRepository
import ch.abwesend.privatecontacts.domain.repository.ISyncStateRepository
import ch.abwesend.privatecontacts.domain.service.ContactSanitizingService
import ch.abwesend.privatecontacts.domain.service.SyncConflictResolution
import ch.abwesend.privatecontacts.domain.service.interfaces.ISyncContactMapper
import ch.abwesend.privatecontacts.domain.util.deterministicUuid
import ch.abwesend.privatecontacts.domain.util.injectAnywhere

/**
 * Applies a single decrypted [SyncCommandEnvelope] to local state, running the deterministic
 * conflict/fork logic. Kept independent of the Drive plumbing so it is unit-testable.
 *
 * Upserts are applied as delete-then-create: the shared [IContactRepository] already persists the
 * contact row, contact-data, groups, and image atomically, so this reuses it without re-enqueuing
 * (it bypasses ContactSaveService).
 */
class SyncApplyService {
    private val syncStateRepository: ISyncStateRepository by injectAnywhere()
    private val conflictRepository: ISyncConflictRepository by injectAnywhere()
    private val outboxRepository: ISyncOutboxRepository by injectAnywhere()
    private val contactRepository: IContactRepository by injectAnywhere()
    private val contactMapper: ISyncContactMapper by injectAnywhere()
    private val sanitizingService: ContactSanitizingService by injectAnywhere()

    private companion object {
        const val SNAPSHOT_DEVICE = "snapshot"
    }

    suspend fun applyEnvelope(envelope: SyncCommandEnvelope) {
        val guid = envelope.contactSyncId
        val state = syncStateRepository.getBySyncGuid(guid)
        when (val command = envelope.command) {
            is CreateContactCommand -> applyUpsert(envelope, guid, state, command.contact)
            is UpdateContactCommand -> applyUpsert(envelope, guid, state, command.contact)
            is DeleteContactCommand -> applyDelete(envelope, guid, state)
        }
    }

    private suspend fun applyUpsert(
        envelope: SyncCommandEnvelope,
        guid: String,
        state: ContactSyncState?,
        incomingContact: SyncContact,
    ) {
        when {
            state == null -> {
                val contactId = ContactIdInternal.randomId()
                persistUpsert(contactId, incomingContact)
                syncStateRepository.upsert(cleanState(contactId, guid, envelope.newRevision, deleted = false))
            }
            state.localRevision == envelope.newRevision -> {
                logger.debug("Sync command for $guid already applied (idempotent)")
            }
            state.deleted -> resolveDeleteVsUpdate(
                envelope = envelope,
                state = state,
                updateRevision = envelope.newRevision,
                updateContent = incomingContact,
                deleteRevision = state.localRevision,
                updateIsIncoming = true,
            )
            canFastForward(envelope, state) -> {
                persistUpsert(state.contactId, incomingContact)
                syncStateRepository.upsert(cleanState(state.contactId, guid, envelope.newRevision, deleted = false))
            }
            else -> resolveUpdateUpdate(envelope, guid, state, incomingContact)
        }
    }

    private suspend fun applyDelete(
        envelope: SyncCommandEnvelope,
        guid: String,
        state: ContactSyncState?,
    ) {
        when {
            state == null -> {
                // unknown guid: record a tombstone so a later out-of-order update cannot resurrect it
                syncStateRepository.upsert(
                    cleanState(ContactIdInternal.randomId(), guid, envelope.newRevision, deleted = true)
                )
            }
            state.localRevision == envelope.newRevision -> {
                logger.debug("Delete for $guid already applied (idempotent)")
            }
            state.deleted -> {
                syncStateRepository.upsert(cleanState(state.contactId, guid, envelope.newRevision, deleted = true))
            }
            canFastForward(envelope, state) -> {
                contactRepository.deleteContacts(listOf(state.contactId))
                syncStateRepository.upsert(cleanState(state.contactId, guid, envelope.newRevision, deleted = true))
            }
            else -> resolveDeleteVsUpdate(
                envelope = envelope,
                state = state,
                updateRevision = state.localRevision,
                updateContent = loadLocalContact(state.contactId, guid),
                deleteRevision = envelope.newRevision,
                updateIsIncoming = false,
            )
        }
    }

    private suspend fun resolveUpdateUpdate(
        envelope: SyncCommandEnvelope,
        guid: String,
        state: ContactSyncState,
        incomingContact: SyncContact,
    ) {
        val incomingWins = SyncConflictResolution.firstCommandWins(
            firstNewRevision = envelope.newRevision,
            firstCommandId = envelope.commandId,
            secondNewRevision = state.localRevision,
            secondCommandId = "",
        )
        if (incomingWins) {
            val losingContent = loadLocalContact(state.contactId, guid)
            persistUpsert(state.contactId, incomingContact)
            syncStateRepository.upsert(cleanState(state.contactId, guid, envelope.newRevision, deleted = false))
            createFork(guid, state.localRevision, losingContent, envelope.deviceId, ConflictKind.UPDATE_UPDATE, state.contactId)
        } else {
            // local wins: g stays; record that the incoming (losing) revision has been observed
            syncStateRepository.upsert(state.copy(lastSyncedRevision = state.localRevision))
            createFork(guid, envelope.newRevision, incomingContact, envelope.deviceId, ConflictKind.UPDATE_UPDATE, state.contactId)
        }
    }

    private suspend fun resolveDeleteVsUpdate(
        envelope: SyncCommandEnvelope,
        state: ContactSyncState,
        updateRevision: String,
        updateContent: SyncContact,
        deleteRevision: String,
        updateIsIncoming: Boolean,
    ) {
        val guid = state.syncGuid
        val deleteWins = SyncConflictResolution.firstCommandWins(
            firstNewRevision = deleteRevision,
            firstCommandId = envelope.commandId,
            secondNewRevision = updateRevision,
            secondCommandId = "",
        )
        if (deleteWins) {
            // delete wins: g stays/becomes deleted; the update is preserved as the fork copy
            contactRepository.deleteContacts(listOf(state.contactId))
            syncStateRepository.upsert(cleanState(state.contactId, guid, deleteRevision, deleted = true))
            createFork(guid, updateRevision, updateContent, envelope.deviceId, ConflictKind.DELETE_UPDATE, localContactId = null)
        } else {
            // update wins: the delete is dropped, the update value is kept (undeleting if necessary)
            persistUpsert(state.contactId, updateContent)
            syncStateRepository.upsert(cleanState(state.contactId, guid, updateRevision, deleted = false))
            if (updateIsIncoming) {
                logger.debug("Update won over a local delete for $guid")
            }
        }
    }

    private suspend fun createFork(
        originalGuid: String,
        losingRevision: String,
        losingContent: SyncContact,
        remoteDeviceId: String,
        kind: ConflictKind,
        localContactId: IContactIdInternal?,
    ) {
        val forkGuid = SyncConflictResolution.forkSyncId(originalGuid, losingRevision)
        val existingFork = syncStateRepository.getBySyncGuid(forkGuid)
        if (existingFork != null) {
            logger.debug("Fork $forkGuid already exists - idempotent create-on-existing")
            return
        }

        val forkRevision = SyncConflictResolution.forkInitialRevision(forkGuid, losingRevision)
        val forkContactId = ContactIdInternal.randomId()
        val forkContent = losingContent.copy(syncId = forkGuid)
        persistUpsert(forkContactId, forkContent)
        syncStateRepository.upsert(
            ContactSyncState(
                contactId = forkContactId,
                syncGuid = forkGuid,
                localRevision = forkRevision,
                lastSyncedRevision = null,
                dirty = true,
                deleted = false,
                deleteSyncedAtUtcMillis = null,
            )
        )
        // enqueue Create(g') for propagation; deterministic id + revision -> concurrent duplicates collapse
        outboxRepository.enqueue(
            SyncOutboxCommand(
                sequenceNo = 0,
                commandId = deterministicUuid(forkGuid, forkRevision).toString(),
                syncGuid = forkGuid,
                operation = SyncCommandOperation.CREATE,
                plaintextPayload = contactMapper.serializeContact(forkContent),
                payloadFileRef = null,
                baseRevision = null,
                newRevision = forkRevision,
                createdAtUtcMillis = System.currentTimeMillis(),
            )
        )
        conflictRepository.upsert(
            SyncConflict(
                syncId = forkGuid,
                localContactId = localContactId,
                forkContactId = forkContactId,
                remoteDeviceId = remoteDeviceId,
                kind = kind,
                detectedAtUtcMillis = System.currentTimeMillis(),
            )
        )
        logger.info("Recorded sync conflict for $originalGuid -> fork $forkGuid ($kind)")
    }

    /** Inserts a snapshot contact if this device has no state for it yet (bootstrap / absent-locally). */
    suspend fun insertSnapshotContact(snapshotContact: SyncSnapshotContact) {
        val guid = snapshotContact.contact.syncId
        if (syncStateRepository.getBySyncGuid(guid) == null) {
            val contactId = ContactIdInternal.randomId()
            persistUpsert(contactId, snapshotContact.contact)
            syncStateRepository.upsert(cleanState(contactId, guid, snapshotContact.revision, deleted = false))
        }
    }

    /**
     * Diffs a snapshot contact against local state (gap-recovery, issue 2): adopt when clean, keep
     * local pending deletes, and run the deterministic conflict/fork path when the local row is dirty.
     */
    suspend fun reconcileSnapshotContact(snapshotContact: SyncSnapshotContact) {
        val guid = snapshotContact.contact.syncId
        val state = syncStateRepository.getBySyncGuid(guid)
        when {
            state == null -> insertSnapshotContact(snapshotContact)
            state.deleted -> logger.debug("Keeping local tombstone for $guid; not resurrecting from snapshot")
            state.localRevision == snapshotContact.revision ->
                logger.debug("Snapshot value for $guid already local")
            !state.dirty -> {
                persistUpsert(state.contactId, snapshotContact.contact)
                syncStateRepository.upsert(cleanState(state.contactId, guid, snapshotContact.revision, deleted = false))
            }
            else -> {
                val snapshotWins = SyncConflictResolution.firstCommandWins(
                    firstNewRevision = snapshotContact.revision,
                    firstCommandId = "",
                    secondNewRevision = state.localRevision,
                    secondCommandId = "",
                )
                if (snapshotWins) {
                    val losingContent = loadLocalContact(state.contactId, guid)
                    persistUpsert(state.contactId, snapshotContact.contact)
                    syncStateRepository.upsert(cleanState(state.contactId, guid, snapshotContact.revision, deleted = false))
                    createFork(guid, state.localRevision, losingContent, SNAPSHOT_DEVICE, ConflictKind.UPDATE_UPDATE, state.contactId)
                } else {
                    createFork(
                        guid,
                        snapshotContact.revision,
                        snapshotContact.contact,
                        SNAPSHOT_DEVICE,
                        ConflictKind.UPDATE_UPDATE,
                        state.contactId,
                    )
                }
            }
        }
    }

    /** Builds a snapshot of all currently-synced (non-deleted) contacts folding only synced state. */
    suspend fun buildSnapshot(
        snapshotId: String,
        createdByDevice: String,
        perDeviceHighWater: Map<String, Long>,
        createdAtUtcMillis: Long,
    ): SyncSnapshot {
        val contacts = syncStateRepository.getAll()
            .filterNot { it.deleted }
            .mapNotNull { state ->
                try {
                    val contact = contactRepository.resolveContact(state.contactId)
                    SyncSnapshotContact(contactMapper.toSyncContact(contact, state.syncGuid), state.localRevision)
                } catch (e: Exception) {
                    logger.warning("Failed to fold contact ${state.syncGuid} into snapshot", e)
                    null
                }
            }
        return SyncSnapshot(
            snapshotId = snapshotId,
            createdByDevice = createdByDevice,
            createdAtUtcMillis = createdAtUtcMillis,
            perDeviceHighWater = perDeviceHighWater,
            contacts = contacts,
        )
    }

    private suspend fun persistUpsert(contactId: IContactIdInternal, syncContact: SyncContact) {
        // delete-then-create so an update fully replaces contact-data/groups/image without duplicating rows
        contactRepository.deleteContacts(listOf(contactId))
        val contact = contactMapper.toContact(syncContact, contactId)
        sanitizingService.sanitizeContact(contact)
        contactRepository.createContact(contactId, contact)
    }

    private suspend fun loadLocalContact(contactId: IContactIdInternal, guid: String): SyncContact {
        val contact = contactRepository.resolveContact(contactId)
        return contactMapper.toSyncContact(contact, guid)
    }

    private fun canFastForward(envelope: SyncCommandEnvelope, state: ContactSyncState): Boolean =
        envelope.baseRevision == state.localRevision ||
            (!state.dirty && envelope.baseRevision == state.lastSyncedRevision)

    private fun cleanState(
        contactId: IContactIdInternal,
        guid: String,
        revision: String,
        deleted: Boolean,
    ): ContactSyncState =
        ContactSyncState(
            contactId = contactId,
            syncGuid = guid,
            localRevision = revision,
            lastSyncedRevision = revision,
            dirty = false,
            deleted = deleted,
            deleteSyncedAtUtcMillis = if (deleted) System.currentTimeMillis() else null,
        )
}
