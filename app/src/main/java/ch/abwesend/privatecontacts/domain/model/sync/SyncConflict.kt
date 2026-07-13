/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.sync

import ch.abwesend.privatecontacts.domain.model.contact.IContactIdInternal

/**
 * A materialized conflict the user should reconcile: the deterministic winner [localContactId]
 * ("g") and the deterministic fork copy [forkContactId] ("g'").
 */
data class SyncConflict(
    val syncId: String,
    val localContactId: IContactIdInternal?,
    val forkContactId: IContactIdInternal,
    val remoteDeviceId: String,
    val kind: ConflictKind,
    val detectedAtUtcMillis: Long,
)

enum class ConflictKind {
    /** both devices updated the same base independently */
    UPDATE_UPDATE,

    /** one device deleted while another updated; delete won, the update was forked */
    DELETE_UPDATE,
}
