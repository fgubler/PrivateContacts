/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.service

import ch.abwesend.privatecontacts.domain.lib.coroutine.IDispatchers
import ch.abwesend.privatecontacts.domain.lib.flow.ResourceFlow
import ch.abwesend.privatecontacts.domain.lib.flow.combineResource
import ch.abwesend.privatecontacts.domain.model.contact.ContactId
import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.model.contact.IContact
import ch.abwesend.privatecontacts.domain.model.contact.IContactBase
import ch.abwesend.privatecontacts.domain.model.contact.IContactBaseWithAccountInformation
import ch.abwesend.privatecontacts.domain.model.contact.IContactIdExternal
import ch.abwesend.privatecontacts.domain.model.contact.IContactIdInternal
import ch.abwesend.privatecontacts.domain.model.contact.asEditable
import ch.abwesend.privatecontacts.domain.model.search.ContactSearchConfig.All
import ch.abwesend.privatecontacts.domain.model.search.ContactSearchConfig.Query
import ch.abwesend.privatecontacts.domain.repository.IAndroidContactLoadService
import ch.abwesend.privatecontacts.domain.repository.IContactRepository
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class ContactLoadService {
    private val contactRepository: IContactRepository by injectAnywhere()
    private val androidContactService: IAndroidContactLoadService by injectAnywhere()
    private val easterEggService: EasterEggService by injectAnywhere()

    private val dispatchers: IDispatchers by injectAnywhere()

    suspend fun loadFullContactsByType(type: ContactType): List<IContact> =
        when (type) {
            ContactType.SECRET -> contactRepository.loadAllContactsFull()
            ContactType.PUBLIC -> androidContactService.loadAllContactsFull()
        }

    suspend fun loadSecretContacts(): ResourceFlow<List<IContactBase>> =
        contactRepository.loadContactsAsFlow(All)

    private fun loadAndroidContacts(): ResourceFlow<List<IContactBase>> =
        androidContactService.loadContactsAsFlow(All)

    suspend fun searchSecretContacts(query: String): ResourceFlow<List<IContactBase>> {
        easterEggService.checkSearchForEasterEggs(query)
        return if (query.isEmpty()) loadSecretContacts()
        else contactRepository.loadContactsAsFlow(Query(query))
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

    private fun searchAndroidContacts(query: String): ResourceFlow<List<IContactBase>> {
        easterEggService.checkSearchForEasterEggs(query)
        return if (query.isEmpty()) loadAndroidContacts()
        else androidContactService.loadContactsAsFlow(Query(query))
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
            is IContactIdExternal -> androidContactService.resolveContact(contactId)
        }

    suspend fun resolveContacts(contactIds: Collection<ContactId>): List<IContact> = coroutineScope {
        val internalContactIds = contactIds.filterIsInstance<IContactIdInternal>().toSet()
        val externalContactIds = contactIds.filterIsInstance<IContactIdExternal>().toSet()

        val externalContacts = async {
            if (externalContactIds.isEmpty()) emptyList()
            else androidContactService.resolveContacts(externalContactIds)
        }
        val internalContacts = async {
            if (internalContactIds.isEmpty()) emptyList()
            else contactRepository.resolveContacts(internalContactIds)
        }

        internalContacts.await() + externalContacts.await()
    }

    suspend fun resolveContactsWithAccountInformation(
        baseContacts: Collection<IContactBaseWithAccountInformation>
    ): Map<ContactId, IContact?> {
        val baseContactsById = baseContacts.associateBy { it.id }
        val contactIds = baseContactsById.keys
        val resolvedContacts = resolveContacts(contactIds).associateBy { it.id }
        return resolvedContacts.mapValues { (id, resolvedContact) ->
            baseContactsById[id]?.let { correspondingContact ->
                resolvedContact.asEditable().also { editableContact ->
                    editableContact.saveInAccount = correspondingContact.saveInAccount
                }
            }
        }
    }

    suspend fun filterForExistingContacts(contactIds: Collection<IContactIdInternal>): Set<IContactIdInternal> =
        contactRepository.filterForExisting(contactIds)
}
