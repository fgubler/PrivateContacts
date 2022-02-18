package ch.abwesend.privatecontacts.view.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.abwesend.privatecontacts.domain.lib.flow.mutableResourceStateFlow
import ch.abwesend.privatecontacts.domain.lib.flow.withLoadingState
import ch.abwesend.privatecontacts.domain.model.contact.IContact
import ch.abwesend.privatecontacts.domain.model.contact.IContactBase
import ch.abwesend.privatecontacts.domain.service.ContactLoadService
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ContactDetailViewModel : ViewModel() {
    private val loadService: ContactLoadService by injectAnywhere()

    private var latestSelectedContact: IContactBase? = null

    private val _selectedContact = mutableResourceStateFlow<IContact>()
    val selectedContact = _selectedContact.asStateFlow()

    fun selectContact(contact: IContactBase) {
        latestSelectedContact = contact
        viewModelScope.launch {
            _selectedContact.withLoadingState {
                loadService.resolveContact(contact)
            }
        }
    }

    fun reloadContact(contact: IContactBase? = latestSelectedContact) {
        contact?.let { selectContact(it) }
    }
}
