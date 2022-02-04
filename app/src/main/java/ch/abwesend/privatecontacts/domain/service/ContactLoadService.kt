package ch.abwesend.privatecontacts.domain.service

import androidx.paging.PagingData
import ch.abwesend.privatecontacts.domain.model.contact.IContact
import ch.abwesend.privatecontacts.domain.model.contact.IContactBase
import ch.abwesend.privatecontacts.domain.model.search.ContactSearchConfig
import ch.abwesend.privatecontacts.domain.repository.ContactPagerFactory
import ch.abwesend.privatecontacts.domain.repository.IContactRepository
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import kotlinx.coroutines.flow.Flow

class ContactLoadService {
    private val contactRepository: IContactRepository by injectAnywhere()
    private val contactPagerFactory: ContactPagerFactory by injectAnywhere()

    fun loadContacts(): Flow<PagingData<IContactBase>> =
        contactPagerFactory.createContactPager(ContactSearchConfig.All).flow

    fun searchContacts(query: String): Flow<PagingData<IContactBase>> =
        contactPagerFactory.createContactPager(ContactSearchConfig.Query(query)).flow

    suspend fun resolveContact(contact: IContactBase): IContact =
        contactRepository.resolveContact(contact)
}
