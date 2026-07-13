/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.service

import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.contact.IContact
import ch.abwesend.privatecontacts.domain.model.contact.IContactIdInternal
import ch.abwesend.privatecontacts.domain.model.sync.ContactSyncState
import ch.abwesend.privatecontacts.domain.model.sync.SyncCommandOperation
import ch.abwesend.privatecontacts.domain.model.sync.SyncOutboxCommand
import ch.abwesend.privatecontacts.domain.repository.ISyncOutboxRepository
import ch.abwesend.privatecontacts.domain.repository.ISyncStateRepository
import ch.abwesend.privatecontacts.domain.service.interfaces.ISyncCommandService
import ch.abwesend.privatecontacts.domain.service.interfaces.ISyncContactMapper
import ch.abwesend.privatecontacts.domain.service.interfaces.ISyncScheduler
import ch.abwesend.privatecontacts.domain.settings.Settings
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import java.util.UUID

class SyncCommandService : ISyncCommandService {
    private val syncStateRepository: ISyncStateRepository by injectAnywhere()
    private val outboxRepository: ISyncOutboxRepository by injectAnywhere()
    private val syncScheduler: ISyncScheduler by injectAnywhere()
    private val contactMapper: ISyncContactMapper by injectAnywhere()

    override suspend fun enqueueUpsert(
        contactId: IContactIdInternal,
        contact: IContact,
        operation: SyncCommandOperation,
    ) {
        if (!Settings.current.syncEnabled) {
            return
        }

        val existingState = syncStateRepository.getByContactId(contactId)
        // if there is no sync-state yet (e.g. contact predates sync being enabled), treat as create
        val effectiveOperation = if (existingState == null) SyncCommandOperation.CREATE else operation
        val syncGuid = existingState?.syncGuid ?: UUID.randomUUID().toString()
        val baseRevision = if (effectiveOperation == SyncCommandOperation.CREATE) {
            null
        } else {
            existingState?.lastSyncedRevision
        }
        val newRevision = UUID.randomUUID().toString()

        val syncContact = contactMapper.toSyncContact(contact, syncGuid)
        val payload = contactMapper.serializeContact(syncContact)

        outboxRepository.enqueue(
            SyncOutboxCommand(
                sequenceNo = 0,
                commandId = UUID.randomUUID().toString(),
                syncGuid = syncGuid,
                operation = effectiveOperation,
                plaintextPayload = payload,
                payloadFileRef = null,
                baseRevision = baseRevision,
                newRevision = newRevision,
                createdAtUtcMillis = System.currentTimeMillis(),
            )
        )
        syncStateRepository.upsert(
            ContactSyncState(
                contactId = contactId,
                syncGuid = syncGuid,
                localRevision = newRevision,
                lastSyncedRevision = existingState?.lastSyncedRevision,
                dirty = true,
                deleted = false,
                deleteSyncedAtUtcMillis = null,
            )
        )
        logger.debug("Enqueued $effectiveOperation sync command for contact $contactId")
        syncScheduler.scheduleUploadDebounced()
    }

    override suspend fun enqueueDeletes(contactIds: Collection<IContactIdInternal>) {
        if (!Settings.current.syncEnabled) {
            return
        }

        var enqueuedAny = false
        contactIds.forEach { contactId ->
            // resolve the GUID BEFORE writing the tombstone
            val existingState = syncStateRepository.getByContactId(contactId)
            if (existingState != null && !existingState.deleted) {
                val newRevision = UUID.randomUUID().toString()
                outboxRepository.enqueue(
                    SyncOutboxCommand(
                        sequenceNo = 0,
                        commandId = UUID.randomUUID().toString(),
                        syncGuid = existingState.syncGuid,
                        operation = SyncCommandOperation.DELETE,
                        plaintextPayload = "",
                        payloadFileRef = null,
                        baseRevision = existingState.lastSyncedRevision,
                        newRevision = newRevision,
                        createdAtUtcMillis = System.currentTimeMillis(),
                    )
                )
                // keep the row as a tombstone so delete-vs-update stays detectable
                syncStateRepository.upsert(
                    existingState.copy(
                        localRevision = newRevision,
                        dirty = true,
                        deleted = true,
                    )
                )
                enqueuedAny = true
                logger.debug("Enqueued DELETE sync command for contact $contactId")
            }
        }
        if (enqueuedAny) {
            syncScheduler.scheduleUploadDebounced()
        }
    }
}
