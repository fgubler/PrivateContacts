/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.room.sync

import ch.abwesend.privatecontacts.domain.model.contact.ContactIdInternal
import ch.abwesend.privatecontacts.domain.model.sync.ContactSyncState
import ch.abwesend.privatecontacts.domain.model.sync.SyncConflict
import ch.abwesend.privatecontacts.domain.model.sync.SyncCursor
import ch.abwesend.privatecontacts.domain.model.sync.SyncOutboxCommand

fun ContactSyncStateEntity.toDomain(): ContactSyncState =
    ContactSyncState(
        contactId = ContactIdInternal(contactId),
        syncGuid = syncGuid,
        localRevision = localRevision,
        lastSyncedRevision = lastSyncedRevision,
        dirty = dirty,
        deleted = deleted,
        deleteSyncedAtUtcMillis = deleteSyncedAtUtcMillis,
    )

fun ContactSyncState.toEntity(): ContactSyncStateEntity =
    ContactSyncStateEntity(
        contactId = contactId.uuid,
        syncGuid = syncGuid,
        localRevision = localRevision,
        lastSyncedRevision = lastSyncedRevision,
        dirty = dirty,
        deleted = deleted,
        deleteSyncedAtUtcMillis = deleteSyncedAtUtcMillis,
    )

fun SyncOutboxEntity.toDomain(): SyncOutboxCommand =
    SyncOutboxCommand(
        sequenceNo = sequenceNo,
        commandId = commandId,
        syncGuid = syncGuid,
        operation = operation,
        plaintextPayload = plaintextPayload,
        payloadFileRef = payloadFileRef,
        baseRevision = baseRevision,
        newRevision = newRevision,
        createdAtUtcMillis = createdAtUtcMillis,
    )

fun SyncOutboxCommand.toEntity(): SyncOutboxEntity =
    SyncOutboxEntity(
        sequenceNo = sequenceNo,
        commandId = commandId,
        syncGuid = syncGuid,
        operation = operation,
        plaintextPayload = plaintextPayload,
        payloadFileRef = payloadFileRef,
        baseRevision = baseRevision,
        newRevision = newRevision,
        createdAtUtcMillis = createdAtUtcMillis,
    )

fun SyncCursorEntity.toDomain(): SyncCursor =
    SyncCursor(
        deviceId = deviceId,
        lastAppliedSequenceNo = lastAppliedSequenceNo,
        updatedAtUtcMillis = updatedAtUtcMillis,
    )

fun SyncCursor.toEntity(): SyncCursorEntity =
    SyncCursorEntity(
        deviceId = deviceId,
        lastAppliedSequenceNo = lastAppliedSequenceNo,
        updatedAtUtcMillis = updatedAtUtcMillis,
    )

fun SyncConflictEntity.toDomain(): SyncConflict =
    SyncConflict(
        syncId = syncGuid,
        localContactId = localContactId?.let { ContactIdInternal(it) },
        forkContactId = ContactIdInternal(forkContactId),
        remoteDeviceId = remoteDeviceId,
        kind = conflictKind,
        detectedAtUtcMillis = detectedAtUtcMillis,
    )

fun SyncConflict.toEntity(): SyncConflictEntity =
    SyncConflictEntity(
        syncGuid = syncId,
        localContactId = localContactId?.uuid,
        forkContactId = forkContactId.uuid,
        remoteDeviceId = remoteDeviceId,
        conflictKind = kind,
        detectedAtUtcMillis = detectedAtUtcMillis,
    )
