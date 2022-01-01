package ch.abwesend.privatecontacts.domain.service

import androidx.paging.PagingData
import ch.abwesend.privatecontacts.domain.model.contact.Contact
import ch.abwesend.privatecontacts.domain.model.contact.ContactBase
import ch.abwesend.privatecontacts.domain.repository.ContactPagerFactory
import ch.abwesend.privatecontacts.domain.repository.IContactRepository
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import kotlinx.coroutines.flow.Flow

class ContactLoadService {
    private val contactRepository: IContactRepository by injectAnywhere()
    private val contactPagerFactory: ContactPagerFactory by injectAnywhere()

    fun loadPagedContacts(): Flow<PagingData<ContactBase>> =
        contactPagerFactory.createContactPager().flow

    suspend fun loadContacts(): List<ContactBase> =
        contactRepository.loadContacts()

    suspend fun resolveContact(contact: ContactBase): Contact =
        contactRepository.resolveContact(contact)
}
