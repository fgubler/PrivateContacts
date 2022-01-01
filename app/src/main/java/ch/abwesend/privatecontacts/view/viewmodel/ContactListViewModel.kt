package ch.abwesend.privatecontacts.view.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import ch.abwesend.privatecontacts.domain.lib.flow.ResourceStateFlow
import ch.abwesend.privatecontacts.domain.lib.flow.mutableResourceStateFlow
import ch.abwesend.privatecontacts.domain.lib.flow.withLoadingState
import ch.abwesend.privatecontacts.domain.model.contact.ContactBase
import ch.abwesend.privatecontacts.domain.service.ContactLoadService
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class ContactListViewModel : ViewModel() {
    private val loadService: ContactLoadService by injectAnywhere()

    private val _contacts = mutableResourceStateFlow<List<ContactBase>>()
    val contacts: ResourceStateFlow<List<ContactBase>> = _contacts

    val contactsPaged: Flow<PagingData<ContactBase>> by lazy {
        loadService.loadPagedContacts()
    }

    // TODO remove
    fun loadContacts() {
        viewModelScope.launch {
            _contacts.withLoadingState { loadService.loadContacts() }
        }
    }

    // TODO it is not very nice to expose the suspend function
    suspend fun resolveContact(contact: ContactBase) =
        loadService.resolveContact(contact)
}
