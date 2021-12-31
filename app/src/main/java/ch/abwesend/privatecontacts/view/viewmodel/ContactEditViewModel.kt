package ch.abwesend.privatecontacts.view.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.abwesend.privatecontacts.domain.lib.flow.EventFlow
import ch.abwesend.privatecontacts.domain.model.contact.Contact
import ch.abwesend.privatecontacts.domain.model.contact.ContactFull
import ch.abwesend.privatecontacts.domain.model.contact.asFull
import ch.abwesend.privatecontacts.domain.model.contact.createNew
import ch.abwesend.privatecontacts.domain.model.result.ContactSaveResult
import ch.abwesend.privatecontacts.domain.service.IContactSaveService
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class ContactEditViewModel : ViewModel() {
    private val saveService: IContactSaveService by injectAnywhere()

    var originalContact: ContactFull? = null
        private set

    var selectedContact: ContactFull? by mutableStateOf(null)
        private set

    private val _saveResult = EventFlow.createShared<ContactSaveResult>()
    val saveResult: Flow<ContactSaveResult> = _saveResult

    fun selectContact(contact: Contact) {
        val full = contact.asFull()
        originalContact = full
        selectedContact = full
    }

    fun createNewContact() {
        val contact = ContactFull.createNew()
        selectContact(contact)
    }

    fun updateContact(contact: ContactFull) {
        selectedContact = contact
    }

    fun saveContact(contact: ContactFull) {
        viewModelScope.launch {
            val result = saveService.saveContact(contact)
            _saveResult.emit(result)
        }
    }

    fun clearContact() {
        selectedContact = null
    }
}
