package ch.abwesend.privatecontacts.view.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import ch.abwesend.privatecontacts.domain.model.contact.Contact
import ch.abwesend.privatecontacts.domain.model.contact.ContactFull
import ch.abwesend.privatecontacts.domain.model.contact.asFull
import ch.abwesend.privatecontacts.domain.model.contact.createNew

class ContactEditViewModel : ViewModel() {
    var selectedContact: ContactFull? by mutableStateOf(null)
        private set

    fun selectContact(contact: Contact) {
        selectedContact = contact.asFull()
    }

    fun createNewContact() {
        val contact = ContactFull.createNew()
        selectContact(contact)
    }

    fun updateContact(contact: ContactFull) {
        selectedContact = contact
    }

    fun saveContact(contact: Contact) {
        // TODO implement
    }

    fun clearContact() {
        selectedContact = null
    }
}
