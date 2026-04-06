/*
 * Private Contacts
 * Copyright (c) 2026.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository.contactsandroid.service

import ch.abwesend.privatecontacts.domain.lib.coroutine.mapAsyncChunked
import ch.abwesend.privatecontacts.domain.lib.flow.ResourceFlow
import ch.abwesend.privatecontacts.domain.lib.flow.toResourceFlow
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.contact.ContactIdAndroid
import ch.abwesend.privatecontacts.domain.model.contact.ContactWithPhoneNumbers
import ch.abwesend.privatecontacts.domain.model.contact.IContact
import ch.abwesend.privatecontacts.domain.model.contact.IContactBase
import ch.abwesend.privatecontacts.domain.model.contact.IContactIdExternal
import ch.abwesend.privatecontacts.domain.model.contactgroup.ContactGroup
import ch.abwesend.privatecontacts.domain.model.search.ContactSearchConfig
import ch.abwesend.privatecontacts.domain.model.search.ContactSearchConfig.All
import ch.abwesend.privatecontacts.domain.model.search.ContactSearchConfig.Query
import ch.abwesend.privatecontacts.domain.repository.IAndroidContactLoadService
import ch.abwesend.privatecontacts.domain.service.ContactValidationService
import ch.abwesend.privatecontacts.domain.service.valid
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import ch.abwesend.privatecontacts.infrastructure.repository.contactsandroid.mapping.ContactsAndroidContactMapper
import ch.abwesend.privatecontacts.infrastructure.repository.contactsandroid.mapping.toContactGroup
import ch.abwesend.privatecontacts.infrastructure.repository.contactsandroid.repository.ContactsAndroidLoadRepository
import contacts.core.entities.Contact
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlin.system.measureTimeMillis

/**
 * Implementation of [IAndroidContactLoadService] using the contacts-android library.
 */
class ContactsAndroidLoadService : IAndroidContactLoadService {
    private val loadRepository: ContactsAndroidLoadRepository by injectAnywhere()
    private val contactMapper: ContactsAndroidContactMapper by injectAnywhere()
    private val validationService: ContactValidationService by injectAnywhere()

    override fun loadContactsAsFlow(searchConfig: ContactSearchConfig): ResourceFlow<List<IContactBase>> =
        when (searchConfig) {
            is All -> loadContacts()
            is Query -> searchContacts(searchConfig.query)
        }

    override suspend fun loadAllContactsFull(): List<IContact> {
        val contactsRaw = loadRepository.loadAllFullContactsRaw()
        return contactsRaw.mapNotNull { contactRaw ->
            val contactId = ContactIdAndroid(contactNo = contactRaw.id, lookupKey = contactRaw.lookupKey)
            try {
                resolveContact(contactId, contactRaw)
            } catch (e: Exception) {
                logger.warning("Failed to resolve contact $contactId during full load")
                null
            }
        }
    }

    override suspend fun resolveContact(contactId: IContactIdExternal): IContact {
        val contactRaw = loadRepository.resolveContactRaw(contactId)
        return resolveContact(contactId, contactRaw)
    }

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

    override suspend fun getAllContactGroups(): List<ContactGroup> =
        loadRepository.loadAllContactGroups()
            .map { it.toContactGroup() }
            .distinctBy { it.id.name }
            .sortedBy { it.id.name }

    override suspend fun findContactsWithPhoneNumber(phoneNumber: String): List<ContactWithPhoneNumbers> {
        val contacts = loadRepository.findContactsWithPhoneNumber(phoneNumber)
        return contacts
            .mapNotNull { contactMapper.toContactWithPhoneNumbers(it, rethrowExceptions = false) }
            .filter { validationService.validateContactBase(it).valid }
    }

    private suspend fun resolveContact(contactId: IContactIdExternal, contactRaw: Contact): IContact {
        val contactGroups = loadRepository.loadContactGroups(contactRaw)

        return contactMapper.toContact(
            contact = contactRaw,
            groups = contactGroups,
            rethrowExceptions = true,
        ) ?: throw IllegalStateException("Could not convert contact $contactId to local data-model (contacts-android)")
    }

    private fun loadContacts(): ResourceFlow<List<IContactBase>> = flow {
        measureTimeMillis {
            val contacts = loadRepository.createContactsBaseFlow()
                .map { contacts ->
                    contacts.filter { validationService.validateContactBase(it).valid }
                }
            emitAll(contacts)
        }.also { duration -> logger.debug("Loading contacts via contacts-android took $duration ms") }
    }.toResourceFlow()

    private fun searchContacts(query: String): ResourceFlow<List<IContactBase>> = flow {
        measureTimeMillis {
            val contacts = loadRepository.createContactsBaseFlow(searchQuery = query).firstOrNull()
            emit(contacts.orEmpty())
        }.also { duration -> logger.debug("Searching contacts via contacts-android for query '$query' took $duration ms") }
    }.toResourceFlow()
}
