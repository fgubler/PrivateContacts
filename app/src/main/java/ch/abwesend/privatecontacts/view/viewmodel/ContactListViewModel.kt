package ch.abwesend.privatecontacts.view.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.abwesend.privatecontacts.domain.lib.flow.ResourceStateFlow
import ch.abwesend.privatecontacts.domain.lib.flow.mutableResourceStateFlow
import ch.abwesend.privatecontacts.domain.lib.flow.withLoadingState
import ch.abwesend.privatecontacts.domain.model.contact.ContactBase
import ch.abwesend.privatecontacts.domain.service.IContactLoadService
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import kotlinx.coroutines.launch

class ContactListViewModel : ViewModel() {
    private val loadService: IContactLoadService by injectAnywhere()

    private val _contacts = mutableResourceStateFlow<List<ContactBase>>()
    val contacts: ResourceStateFlow<List<ContactBase>> = _contacts

    fun loadContacts() {
        viewModelScope.launch {
            _contacts.withLoadingState { loadService.loadContacts() }
        }
    }

    fun createContact() {
        // TODO implement
    }
}
