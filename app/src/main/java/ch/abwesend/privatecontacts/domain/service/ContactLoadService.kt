package ch.abwesend.privatecontacts.domain.service

import ch.abwesend.privatecontacts.domain.model.ContactBase
import ch.abwesend.privatecontacts.domain.repository.IContactRepository
import ch.abwesend.privatecontacts.domain.util.injectAnywhere

interface IContactLoadService {
    suspend fun loadContacts(): List<ContactBase>
}

class ContactLoadService : IContactLoadService {
    private val contactRepository: IContactRepository by injectAnywhere()

    override suspend fun loadContacts(): List<ContactBase> =
        contactRepository.loadContacts()
}
