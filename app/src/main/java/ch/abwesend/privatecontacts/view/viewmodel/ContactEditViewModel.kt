package ch.abwesend.privatecontacts.view.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import ch.abwesend.privatecontacts.domain.model.contact.Contact
import ch.abwesend.privatecontacts.domain.model.contact.ContactEditable
import ch.abwesend.privatecontacts.domain.model.contact.asEditable
import ch.abwesend.privatecontacts.domain.model.contact.createNew

class ContactEditViewModel : ViewModel() {
    private val _contact: MutableState<ContactEditable?> = mutableStateOf(null)
    val contact: State<ContactEditable?> = _contact

    fun selectContact(contact: Contact) {
        _contact.value = contact.asEditable()
    }

    fun createNewContact() {
        val contact = ContactEditable.createNew()
        selectContact(contact)
    }

    fun saveContact(contact: Contact) {
        // TODO implement
    }

    fun clearContact() {
        _contact.value = null
    }
}
