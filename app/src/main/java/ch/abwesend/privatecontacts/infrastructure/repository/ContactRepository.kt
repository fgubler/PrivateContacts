package ch.abwesend.privatecontacts.infrastructure.repository

import ch.abwesend.privatecontacts.domain.model.contact.ContactBase
import ch.abwesend.privatecontacts.domain.model.contact.ContactFull
import ch.abwesend.privatecontacts.domain.repository.IContactRepository

class ContactRepository : RepositoryBase(), IContactRepository {
    override suspend fun loadContacts(): List<ContactBase> =
        withDatabase { database ->
            database.contactDao().getAll()
        }

    override suspend fun resolveContact(contact: ContactBase): ContactFull {
        TODO("Not yet implemented")
    }

    override suspend fun createContact(): ContactFull {
        TODO("Not yet implemented")
    }
}
