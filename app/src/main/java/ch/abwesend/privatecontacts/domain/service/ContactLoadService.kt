/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.service

import androidx.paging.Pager
import androidx.paging.PagingData
import ch.abwesend.privatecontacts.domain.model.contact.IContact
import ch.abwesend.privatecontacts.domain.model.contact.IContactBase
import ch.abwesend.privatecontacts.domain.model.contact.IContactIdExternal
import ch.abwesend.privatecontacts.domain.model.contact.IContactIdInternal
import ch.abwesend.privatecontacts.domain.model.search.ContactSearchConfig
import ch.abwesend.privatecontacts.domain.repository.ContactPagerFactory
import ch.abwesend.privatecontacts.domain.repository.IAndroidContactRepository
import ch.abwesend.privatecontacts.domain.repository.IContactRepository
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import kotlinx.coroutines.flow.Flow

class ContactLoadService {
    private val contactRepository: IContactRepository by injectAnywhere()
    private val androidContactRepository: IAndroidContactRepository by injectAnywhere()
    private val contactPagerFactory: ContactPagerFactory by injectAnywhere()
    private val easterEggService: EasterEggService by injectAnywhere()

    fun loadSecretContacts(): Flow<PagingData<IContactBase>> =
        contactPagerFactory.createSecretContactPager(ContactSearchConfig.All).loadContacts()

    fun searchSecretContacts(query: String): Flow<PagingData<IContactBase>> =
        contactPagerFactory.createSecretContactPager(ContactSearchConfig.Query(query)).searchContacts(query)

    fun loadAllContacts(): Flow<PagingData<IContactBase>> =
        contactPagerFactory.createAllContactPager(ContactSearchConfig.All).loadContacts()

    fun searchAllContacts(query: String): Flow<PagingData<IContactBase>> =
        contactPagerFactory.createAllContactPager(ContactSearchConfig.Query(query)).searchContacts(query)

    suspend fun resolveContact(contact: IContactBase): IContact =
        contact.id.let { contactId ->
            when (contactId) {
                is IContactIdInternal -> contactRepository.resolveContact(contactId)
                is IContactIdExternal -> TODO("Resolving android contacts is not yet implemented")
            }
        }

    private fun Pager<Int, IContactBase>.loadContacts(): Flow<PagingData<IContactBase>> = flow

    private fun Pager<Int, IContactBase>.searchContacts(query: String): Flow<PagingData<IContactBase>> {
        easterEggService.checkSearchForEasterEggs(query)
        return flow
    }
}
