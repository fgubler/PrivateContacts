package ch.abwesend.privatecontacts.domain.service

import ch.abwesend.privatecontacts.domain.model.contact.Contact
import ch.abwesend.privatecontacts.domain.model.contact.ContactBase
import ch.abwesend.privatecontacts.domain.repository.IContactRepository
import ch.abwesend.privatecontacts.domain.util.injectAnywhere

interface IContactLoadService {
    suspend fun loadContacts(): List<ContactBase>
    suspend fun resolveContact(contact: ContactBase): Contact
}

class ContactLoadService : IContactLoadService {
    private val contactRepository: IContactRepository by injectAnywhere()

    override suspend fun loadContacts(): List<ContactBase> =
        contactRepository.loadContacts()

    override suspend fun resolveContact(contact: ContactBase): Contact =
        contactRepository.resolveContact(contact)
}
