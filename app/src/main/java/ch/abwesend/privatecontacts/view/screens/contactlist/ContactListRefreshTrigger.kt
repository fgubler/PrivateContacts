/*
 * Private Contacts
 * Copyright (c) 2026.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.screens.contactlist

import ch.abwesend.privatecontacts.domain.lib.flow.EventFlow
import kotlinx.coroutines.flow.Flow

/**
 * Allows code outside of the contact-list screen (e.g. the import of a vcf-file opened with the app)
 * to make the contact-list reload its contacts: its view-model is scoped to its screen and therefore
 * not reachable from the outside.
 */
class ContactListRefreshTrigger {
    private val _refreshRequests = EventFlow.createShared<Unit>()
    val refreshRequests: Flow<Unit> = _refreshRequests

    suspend fun requestRefresh() {
        _refreshRequests.emit(Unit)
    }
}
