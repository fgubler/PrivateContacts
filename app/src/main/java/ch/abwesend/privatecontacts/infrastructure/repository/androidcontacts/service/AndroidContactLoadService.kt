/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.service

import ch.abwesend.privatecontacts.domain.lib.coroutine.mapAsyncChunked
import ch.abwesend.privatecontacts.domain.lib.flow.ResourceFlow
import ch.abwesend.privatecontacts.domain.lib.flow.toResourceFlow
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.contact.ContactAccount
import ch.abwesend.privatecontacts.domain.model.contact.ContactIdAndroid
import ch.abwesend.privatecontacts.domain.model.contact.IContact
import ch.abwesend.privatecontacts.domain.model.contact.IContactBase
import ch.abwesend.privatecontacts.domain.model.contact.IContactIdExternal
import ch.abwesend.privatecontacts.domain.model.contactgroup.ContactGroup
import ch.abwesend.privatecontacts.domain.model.search.ContactSearchConfig
import ch.abwesend.privatecontacts.domain.model.search.ContactSearchConfig.All
import ch.abwesend.privatecontacts.domain.model.search.ContactSearchConfig.Query
import ch.abwesend.privatecontacts.domain.repository.IAndroidContactLoadService
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.mapping.AndroidContactMapper
import ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.mapping.toContactAccount
import ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.mapping.toContactGroup
import ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.repository.AndroidContactLoadRepository
import com.alexstyl.contactstore.Contact
import com.alexstyl.contactstore.ContactPredicate
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlin.system.measureTimeMillis

/**
 * Repository to access the android ContactsProvider
 */
class AndroidContactLoadService : IAndroidContactLoadService {
    private val contactLoadRepository: AndroidContactLoadRepository by injectAnywhere()
    private val contactFactory: AndroidContactMapper by injectAnywhere()

    override fun loadContactsAsFlow(searchConfig: ContactSearchConfig): ResourceFlow<List<IContactBase>> =
        when (searchConfig) {
            is All -> loadContacts()
            is Query -> searchContacts(searchConfig.query)
        }

    // TODO add unit tests
    override suspend fun loadAllContactsFull(): List<IContact> {
        val contactsRaw = contactLoadRepository.loadAllFullContactsRaw()
        return contactsRaw.map { contactRaw ->
            val contactId = ContactIdAndroid(contactNo = contactRaw.contactId)
            resolveContact(contactId, contactRaw)
        }
    }

    override suspend fun resolveContact(contactId: IContactIdExternal): IContact {
        val contactRaw = contactLoadRepository.resolveContactRaw(contactId)
        return resolveContact(contactId, contactRaw)
    }

    // TODO add proper batch-processing and then unit-tests
    override suspend fun resolveContacts(contactIds: Set<IContactIdExternal>): List<IContact> {
        return contactIds.mapAsyncChunked { contactId ->
            try {
                resolveContact(contactId)
            } catch (e: Exception) {
                logger.warning("Failed to resolve contact $contactId")
                null
            }
        }.filterNotNull()
    }

    suspend fun resolveContact(contactId: IContactIdExternal, contactRaw: Contact): IContact {
        val contactGroups = contactLoadRepository.loadContactGroups(contactRaw)

        return contactFactory.toContact(
            contact = contactRaw,
            groups = contactGroups,
            rethrowExceptions = true,
        ) ?: throw IllegalStateException("Could not convert contact $contactId to local data-model")
    }

    suspend fun doContactsExist(contactIds: Set<IContactIdExternal>): Map<IContactIdExternal, Boolean> {
        logger.debug("Checking for contact existence of ${contactIds.size} contacts")
        val existingContactIds = contactLoadRepository.loadContactsSnapshot().map { it.id }.toSet()
        val existenceByContactId = contactIds.associateWith { existingContactIds.contains(it) }
        val numberOfExisting = existenceByContactId.filter { it.value }.size
        logger.debug("Checking for contact existence: $numberOfExisting exist")
        return existenceByContactId
    }

    suspend fun getAllContactGroups(): List<ContactGroup> =
        contactLoadRepository.loadAllContactGroups().map { it.toContactGroup() }

    suspend fun getContactGroups(filterForAccount: ContactAccount): List<ContactGroup> =
        contactLoadRepository.loadAllContactGroups()
            .filter { it.account.toContactAccount() == filterForAccount }
            .map { it.toContactGroup() }

    private fun loadContacts(): ResourceFlow<List<IContactBase>> = flow {
        measureTimeMillis {
            val contacts = contactLoadRepository.createContactsBaseFlow()
            emitAll(contacts)
        }.also { duration -> logger.debug("Loading android contacts took $duration ms") }
    }.toResourceFlow()

    private fun searchContacts(query: String): ResourceFlow<List<IContactBase>> = flow {
        measureTimeMillis {
            val predicate = ContactPredicate.ContactLookup(query)
            val contacts = contactLoadRepository.createContactsBaseFlow(predicate).firstOrNull()
            emit(contacts.orEmpty())
        }.also { duration -> logger.debug("Loading android contacts for query '$query' took $duration ms") }
    }.toResourceFlow()
}
