/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.service

import ch.abwesend.privatecontacts.domain.lib.coroutine.IDispatchers
import ch.abwesend.privatecontacts.domain.lib.coroutine.mapAsyncChunked
import ch.abwesend.privatecontacts.domain.lib.flow.ResourceFlow
import ch.abwesend.privatecontacts.domain.lib.flow.combineResource
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.contact.ContactId
import ch.abwesend.privatecontacts.domain.model.contact.IContact
import ch.abwesend.privatecontacts.domain.model.contact.IContactBase
import ch.abwesend.privatecontacts.domain.model.contact.IContactIdExternal
import ch.abwesend.privatecontacts.domain.model.contact.IContactIdInternal
import ch.abwesend.privatecontacts.domain.model.search.ContactSearchConfig.All
import ch.abwesend.privatecontacts.domain.model.search.ContactSearchConfig.Query
import ch.abwesend.privatecontacts.domain.repository.IAndroidContactLoadRepository
import ch.abwesend.privatecontacts.domain.repository.IContactRepository
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

class ContactLoadService {
    private val contactRepository: IContactRepository by injectAnywhere()
    private val androidContactRepository: IAndroidContactLoadRepository by injectAnywhere()
    private val easterEggService: EasterEggService by injectAnywhere()

    private val dispatchers: IDispatchers by injectAnywhere()

    suspend fun loadSecretContacts(): ResourceFlow<List<IContactBase>> =
        contactRepository.getContactsAsFlow(All)

    suspend fun searchSecretContacts(query: String): ResourceFlow<List<IContactBase>> {
        easterEggService.checkSearchForEasterEggs(query)
        return if (query.isEmpty()) loadSecretContacts()
        else contactRepository.getContactsAsFlow(Query(query))
    }

    suspend fun loadAllContacts(): ResourceFlow<List<IContactBase>> = coroutineScope {
        val secretContactsDeferred = async { loadSecretContacts() }
        val androidContactsDeferred = async { loadAndroidContacts() }

        val secretContacts = secretContactsDeferred.await()
        val androidContacts = androidContactsDeferred.await()

        combineContacts(secretContacts, androidContacts)
    }

    suspend fun searchAllContacts(query: String): ResourceFlow<List<IContactBase>> = coroutineScope {
        val secretContactsDeferred = async { searchSecretContacts(query) }
        val androidContactsDeferred = async { searchAndroidContacts(query) }

        val secretContacts = secretContactsDeferred.await()
        val androidContacts = androidContactsDeferred.await()

        combineContacts(secretContacts, androidContacts)
    }

    private fun loadAndroidContacts(): ResourceFlow<List<IContactBase>> =
        androidContactRepository.loadContactsAsFlow(All)

    private fun searchAndroidContacts(query: String): ResourceFlow<List<IContactBase>> {
        easterEggService.checkSearchForEasterEggs(query)
        return if (query.isEmpty()) loadAndroidContacts()
        else androidContactRepository.loadContactsAsFlow(Query(query))
    }

    private fun combineContacts(
        contactsFlow1: ResourceFlow<List<IContactBase>>,
        contactsFlow2: ResourceFlow<List<IContactBase>>
    ): ResourceFlow<List<IContactBase>> = contactsFlow1.combineResource(contactsFlow2) { contacts1, contacts2 ->
        contacts1 + contacts2
    }

    suspend fun resolveContact(contactId: ContactId): IContact =
        when (contactId) {
            is IContactIdInternal -> contactRepository.resolveContact(contactId)
            is IContactIdExternal -> androidContactRepository.resolveContact(contactId)
        }

    suspend fun resolveContacts(contactIds: Collection<ContactId>): Map<ContactId, IContact?> =
        withContext(dispatchers.default) {
            contactIds.mapAsyncChunked { contactId ->
                val contact = try {
                    resolveContact(contactId)
                } catch (e: Exception) {
                    logger.warning("Failed to load contact $contactId", e)
                    null
                }
                contactId to contact
            }.toMap()
        }
}
