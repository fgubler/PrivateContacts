/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.room.sync

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Maps a local secret contact to its cross-device sync GUID + revision state.
 *
 * Deliberately has NO foreign key to ContactEntity: on delete the contact row is removed but this
 * row survives as a tombstone (deleted = true) so delete-vs-update conflicts stay detectable.
 */
@Entity(
    indices = [
        Index(value = ["syncGuid"], unique = true),
    ],
)
data class ContactSyncStateEntity(
    @PrimaryKey val contactId: UUID,
    val syncGuid: String,
    val localRevision: String,
    val lastSyncedRevision: String?,
    val dirty: Boolean,
    val deleted: Boolean,
    val deleteSyncedAtUtcMillis: Long?,
)
