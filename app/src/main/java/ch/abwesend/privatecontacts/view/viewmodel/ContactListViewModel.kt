package ch.abwesend.privatecontacts.view.viewmodel

import androidx.lifecycle.ViewModel
import ch.abwesend.privatecontacts.domain.lib.flow.ResourceStateFlow
import ch.abwesend.privatecontacts.domain.model.Contact
import ch.abwesend.privatecontacts.domain.service.IContactLoadService
import ch.abwesend.privatecontacts.domain.util.injectAnywhere

class ContactListViewModel : ViewModel() {
    private val loadService: IContactLoadService by injectAnywhere()

    // TODO do resources make sense here?
    val contacts: ResourceStateFlow<List<Contact>> by lazy {
        loadService.loadContacts()
    }

    fun createContact() {
        // TODO implement
    }
}
