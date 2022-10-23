package ch.abwesend.privatecontacts.view.model

import ch.abwesend.privatecontacts.domain.model.contact.IContactBase

sealed interface ContactListScreenState {
    object Normal : ContactListScreenState
    data class Search(val searchText: String) : ContactListScreenState
    data class BulkMode(val selectedContacts: Set<IContactBase>) : ContactListScreenState
}
