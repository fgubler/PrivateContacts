/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.service

import ch.abwesend.privatecontacts.domain.lib.flow.ResourceFlow
import ch.abwesend.privatecontacts.domain.model.contact.IContact
import ch.abwesend.privatecontacts.domain.model.contact.IContactBase
import ch.abwesend.privatecontacts.domain.model.contact.IContactIdExternal
import ch.abwesend.privatecontacts.domain.model.contact.IContactIdInternal
import ch.abwesend.privatecontacts.domain.model.search.ContactSearchConfig
import ch.abwesend.privatecontacts.domain.repository.IAndroidContactRepository
import ch.abwesend.privatecontacts.domain.repository.IContactRepository
import ch.abwesend.privatecontacts.domain.util.applicationScope
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import kotlinx.coroutines.launch

class ContactLoadService {
    private val contactRepository: IContactRepository by injectAnywhere()
    private val androidContactRepository: IAndroidContactRepository by injectAnywhere()
    private val easterEggService: EasterEggService by injectAnywhere()

    suspend fun loadSecretContacts(): ResourceFlow<List<IContactBase>> =
        contactRepository.getContactsAsFlow(ContactSearchConfig.All)

    suspend fun searchSecretContacts(query: String): ResourceFlow<List<IContactBase>> {
        easterEggService.checkSearchForEasterEggs(query)
        return if (query.isEmpty()) contactRepository.getContactsAsFlow(ContactSearchConfig.All)
        else contactRepository.getContactsAsFlow(ContactSearchConfig.Query(query))
    }

    suspend fun loadAllContacts(): ResourceFlow<List<IContactBase>> {
        // TODO implement
        applicationScope.launch {
            androidContactRepository.loadContacts() // TODO remove
        }
        return loadSecretContacts()
    }

    suspend fun searchAllContacts(query: String): ResourceFlow<List<IContactBase>> {
        // TODO implement
        return searchSecretContacts(query)
    }

    suspend fun resolveContact(contact: IContactBase): IContact =
        contact.id.let { contactId ->
            when (contactId) {
                is IContactIdInternal -> contactRepository.resolveContact(contactId)
                is IContactIdExternal -> TODO("Resolving android contacts is not yet implemented")
            }
        }
}
