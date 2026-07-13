/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.room.sync

import androidx.room.Entity
import androidx.room.PrimaryKey
import ch.abwesend.privatecontacts.domain.model.sync.ConflictKind
import java.util.UUID

/**
 * A recorded conflict to surface to the user: the deterministic winner [localContactId] ("g") and
 * the materialized fork copy [forkContactId] ("g'"). Both reference real contacts, so no encrypted
 * payload is stored.
 */
@Entity
data class SyncConflictEntity(
    @PrimaryKey val syncGuid: String,
    val localContactId: UUID?,
    val forkContactId: UUID,
    val remoteDeviceId: String,
    val conflictKind: ConflictKind,
    val detectedAtUtcMillis: Long,
)
