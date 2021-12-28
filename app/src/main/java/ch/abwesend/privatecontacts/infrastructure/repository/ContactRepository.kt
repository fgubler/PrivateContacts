package ch.abwesend.privatecontacts.infrastructure.repository

import ch.abwesend.privatecontacts.domain.model.ContactBase
import ch.abwesend.privatecontacts.domain.repository.IContactRepository

class ContactRepository : RepositoryBase(), IContactRepository {
    override suspend fun loadContacts(): List<ContactBase> =
        withDatabase { database ->
            database.contactDao().getAll()
        }
}
