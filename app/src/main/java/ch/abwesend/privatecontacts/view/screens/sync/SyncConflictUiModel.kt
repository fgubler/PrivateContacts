/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.screens.sync

import ch.abwesend.privatecontacts.domain.model.contact.IContactIdInternal

/** a synchronization conflict prepared for display: the winner "original" and the duplicate "copy" */
data class SyncConflictUiModel(
    val syncId: String,
    val originalName: String?,
    val copyName: String,
    val localContactId: IContactIdInternal?,
    val forkContactId: IContactIdInternal,
)
