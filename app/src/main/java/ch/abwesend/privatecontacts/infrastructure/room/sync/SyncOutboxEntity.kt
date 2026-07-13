/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.room.sync

import androidx.room.Entity
import androidx.room.PrimaryKey
import ch.abwesend.privatecontacts.domain.model.sync.SyncCommandOperation

/**
 * A pending outbound command. Holds the PLAINTEXT serialized DTO (never ciphertext) so the upload
 * worker can coalesce superseded updates; encryption happens at upload time. The auto-generated
 * primary key gives a strict upload order.
 */
@Entity
data class SyncOutboxEntity(
    @PrimaryKey(autoGenerate = true) val sequenceNo: Long = 0,
    val commandId: String,
    val syncGuid: String,
    val operation: SyncCommandOperation,
    val plaintextPayload: String,
    val payloadFileRef: String?,
    val baseRevision: String?,
    val newRevision: String,
    val createdAtUtcMillis: Long,
)
