/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.sync

import ch.abwesend.privatecontacts.domain.model.contact.IContactIdInternal

/**
 * Per-contact synchronization state. On delete the contact record is removed but this state
 * survives as a tombstone ([deleted] = true) so delete-vs-update stays detectable.
 */
data class ContactSyncState(
    val contactId: IContactIdInternal,
    val syncGuid: String,
    val localRevision: String,
    val lastSyncedRevision: String?,
    val dirty: Boolean,
    val deleted: Boolean,
    val deleteSyncedAtUtcMillis: Long?,
)

/** a pending outbound command holding the PLAINTEXT DTO (encrypted only at upload time) */
data class SyncOutboxCommand(
    val sequenceNo: Long,
    val commandId: String,
    val syncGuid: String,
    val operation: SyncCommandOperation,
    /** serialized [SyncContact] JSON for create/update; empty for delete */
    val plaintextPayload: String,
    /** reference to an internal-storage file for oversized payloads (large images) */
    val payloadFileRef: String?,
    val baseRevision: String?,
    val newRevision: String,
    val createdAtUtcMillis: Long,
)

/** "applied up to" marker for a remote device */
data class SyncCursor(
    val deviceId: String,
    val lastAppliedSequenceNo: Long,
    val updatedAtUtcMillis: Long,
)
