/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.room.sync

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Per remote-device "applied up to" marker. Local-only; never uploaded. */
@Entity
data class SyncCursorEntity(
    @PrimaryKey val deviceId: String,
    val lastAppliedSequenceNo: Long,
    val updatedAtUtcMillis: Long,
)
