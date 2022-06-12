/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.service

import ch.abwesend.privatecontacts.domain.lib.flow.ReadyResource
import ch.abwesend.privatecontacts.domain.lib.flow.ResourceFlow
import ch.abwesend.privatecontacts.domain.lib.flow.combineResource
import ch.abwesend.privatecontacts.domain.model.contact.IContact
import ch.abwesend.privatecontacts.domain.model.contact.IContactBase
import ch.abwesend.privatecontacts.domain.model.contact.IContactIdExternal
import ch.abwesend.privatecontacts.domain.model.contact.IContactIdInternal
import ch.abwesend.privatecontacts.domain.model.contact.getFullName
import ch.abwesend.privatecontacts.domain.model.search.ContactSearchConfig
import ch.abwesend.privatecontacts.domain.repository.IAndroidContactRepository
import ch.abwesend.privatecontacts.domain.repository.IContactRepository
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.map

class ContactLoadService {
    private val contactRepository: IContactRepository by injectAnywhere()
    private val androidContactRepository: IAndroidContactRepository by injectAnywhere()
    private val easterEggService: EasterEggService by injectAnywhere()

    suspend fun loadSecretContacts(): ResourceFlow<List<IContactBase>> =
        contactRepository.getContactsAsFlow(ContactSearchConfig.All)

    suspend fun searchSecretContacts(query: String): ResourceFlow<List<IContactBase>> {
        easterEggService.checkSearchForEasterEggs(query)
        return if (query.isEmpty()) loadSecretContacts()
        else contactRepository.getContactsAsFlow(ContactSearchConfig.Query(query))
    }

    suspend fun loadAllContacts(): ResourceFlow<List<IContactBase>> = coroutineScope {
        val secretContactsDeferred = async { loadSecretContacts() }
        val androidContactsDeferred = async { androidContactRepository.loadContactsAsFlow() }

        val secretContacts = secretContactsDeferred.await()
        val androidContacts = androidContactsDeferred.await()

        combineContacts(secretContacts, androidContacts)
    }

    suspend fun searchAllContacts(query: String): ResourceFlow<List<IContactBase>> = coroutineScope {
        val secretContactsDeferred = async { searchSecretContacts(query) }
        val androidContactsDeferred = async { androidContactRepository.loadContactsAsFlow() }

        val secretContacts = secretContactsDeferred.await()
        val androidContacts = androidContactsDeferred.await().map { resource ->
            when (resource) {
                is ReadyResource -> {
                    // TODO allow filtering for more than the name
                    val filteredContacts = resource.value.filter {
                        it.getFullName().lowercase().contains(query.lowercase())
                    }
                    ReadyResource(filteredContacts)
                }
                else -> resource
            }
        }

        combineContacts(secretContacts, androidContacts)
    }

    private fun combineContacts(
        contactsFlow1: ResourceFlow<List<IContactBase>>,
        contactsFlow2: ResourceFlow<List<IContactBase>>
    ): ResourceFlow<List<IContactBase>> = contactsFlow1.combineResource(contactsFlow2) { contacts1, contacts2 ->
        val all = contacts1 + contacts2
        all.sortedBy { it.getFullName() }
    }

    suspend fun resolveContact(contact: IContactBase): IContact =
        contact.id.let { contactId ->
            when (contactId) {
                is IContactIdInternal -> contactRepository.resolveContact(contactId)
                is IContactIdExternal -> TODO("Resolving android contacts is not yet implemented")
            }
        }
}
