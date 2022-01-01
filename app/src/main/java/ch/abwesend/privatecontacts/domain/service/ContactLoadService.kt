package ch.abwesend.privatecontacts.domain.service

import ch.abwesend.privatecontacts.domain.model.contact.Contact
import ch.abwesend.privatecontacts.domain.model.contact.ContactBase
import ch.abwesend.privatecontacts.domain.repository.IContactRepository
import ch.abwesend.privatecontacts.domain.util.injectAnywhere

class ContactLoadService {
    private val contactRepository: IContactRepository by injectAnywhere()

    suspend fun loadContacts(): List<ContactBase> =
        contactRepository.loadContacts()

    suspend fun resolveContact(contact: ContactBase): Contact =
        contactRepository.resolveContact(contact)
}
