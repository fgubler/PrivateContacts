/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository

import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.contact.ContactWithPhoneNumbers
import ch.abwesend.privatecontacts.domain.model.contact.IContact
import ch.abwesend.privatecontacts.domain.model.contact.IContactBase
import ch.abwesend.privatecontacts.domain.model.contact.toContactEditable
import ch.abwesend.privatecontacts.domain.model.contact.uuid
import ch.abwesend.privatecontacts.domain.model.result.ContactChangeError.UNKNOWN_ERROR
import ch.abwesend.privatecontacts.domain.model.result.ContactDeleteResult
import ch.abwesend.privatecontacts.domain.model.result.ContactSaveResult
import ch.abwesend.privatecontacts.domain.model.search.ContactSearchConfig
import ch.abwesend.privatecontacts.domain.repository.IContactRepository
import ch.abwesend.privatecontacts.domain.service.FullTextSearchService
import ch.abwesend.privatecontacts.domain.settings.Settings
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import ch.abwesend.privatecontacts.infrastructure.room.contact.toEntity
import ch.abwesend.privatecontacts.infrastructure.room.database.AppDatabase

class ContactRepository : RepositoryBase(), IContactRepository {
    private val contactDataRepository: ContactDataRepository by injectAnywhere()
    private val searchService: FullTextSearchService by injectAnywhere()

    override suspend fun loadContacts(): List<IContactBase> =
        withDatabase { database ->
            database.contactDao().getAll().also {
                logger.info("Loaded ${it.size} contacts")
            }
        }

    override suspend fun getContactsPaged(
        searchConfig: ContactSearchConfig,
        loadSize: Int,
        offsetInRows: Int,
    ): List<IContactBase> = withDatabase { database ->
        logger.info("Loading contacts with pageSize = $loadSize and offset = $offsetInRows")

        val result = when (searchConfig) {
            is ContactSearchConfig.All -> database.getAllContactsPaged(loadSize = loadSize, offsetInRows = offsetInRows)
            is ContactSearchConfig.Query -> database.searchContactsPaged(
                config = searchConfig,
                loadSize = loadSize,
                offsetInRows = offsetInRows,
            )
        }

        logger.info("Loaded ${result.size} contacts with pageSize = $loadSize and offset = $offsetInRows")
        result
    }

    override suspend fun findContactsWithNumberEndingOn(endOfPhoneNumber: String): List<ContactWithPhoneNumbers> {
        val contactData = contactDataRepository.findPhoneNumbersEndingOn(endOfPhoneNumber)
        return withDatabase { database ->
            val contactIds = contactData.keys.map { it.uuid }
            val contacts = database.contactDao().findByIds(contactIds)

            contacts.map { contactEntity ->
                ContactWithPhoneNumbers(
                    contactBase = contactEntity,
                    phoneNumbers = contactData[contactEntity.id].orEmpty()
                )
            }
        }
    }

    private suspend fun AppDatabase.getAllContactsPaged(loadSize: Int, offsetInRows: Int): List<IContactBase> =
        if (Settings.repository.orderByFirstName) {
            contactDao().getPagedByFirstName(loadSize = loadSize, offsetInRows = offsetInRows)
        } else {
            contactDao().getPagedByLastName(loadSize = loadSize, offsetInRows = offsetInRows)
        }

    private suspend fun AppDatabase.searchContactsPaged(
        config: ContactSearchConfig.Query,
        loadSize: Int,
        offsetInRows: Int
    ): List<IContactBase> {
        val phoneNumberQuery = searchService.prepareQueryForPhoneNumberSearch(config.query)
            .takeIf { searchService.isLongEnough(it) }.orEmpty()

        return if (Settings.repository.orderByFirstName) {
            contactDao().searchPagedByFirstName(
                query = config.query,
                phoneNumberQuery = phoneNumberQuery,
                loadSize = loadSize,
                offsetInRows = offsetInRows,
            )
        } else {
            contactDao().searchPagedByLastName(
                query = config.query,
                phoneNumberQuery = phoneNumberQuery,
                loadSize = loadSize,
                offsetInRows = offsetInRows,
            )
        }
    }

    override suspend fun resolveContact(contact: IContactBase): IContact {
        val refreshedContact = withDatabase { database ->
            database.contactDao().findById(contact.uuid)
        } ?: contact
        val contactData = contactDataRepository.loadContactData(contact)
        val resolvedData = contactData.mapNotNull { contactDataRepository.tryResolveContactData(it) }

        return refreshedContact.toContactEditable(contactDataSet = resolvedData.toMutableList())
    }

    override suspend fun createContact(contact: IContact): ContactSaveResult =
        try {
            withDatabase { database ->
                database.contactDao().insert(contact.toEntity())
                contactDataRepository.createContactData(contact)
                ContactSaveResult.Success
            }
        } catch (e: Exception) {
            logger.error("Failed to create contact ${contact.id}", e)
            ContactSaveResult.Failure(UNKNOWN_ERROR)
        }

    override suspend fun updateContact(contact: IContact): ContactSaveResult =
        try {
            withDatabase { database ->
                database.contactDao().update(contact.toEntity())
                contactDataRepository.updateContactData(contact)
                ContactSaveResult.Success
            }
        } catch (e: Exception) {
            logger.error("Failed to update contact ${contact.id}", e)
            ContactSaveResult.Failure(UNKNOWN_ERROR)
        }

    override suspend fun deleteContact(contact: IContactBase): ContactDeleteResult =
        try {
            withDatabase { database ->
                contactDataRepository.deleteContactData(contact)
                database.contactDao().delete(contact.id.uuid)
                ContactDeleteResult.Success
            }
        } catch (e: Exception) {
            logger.error("Failed to delete contact ${contact.id}", e)
            ContactDeleteResult.Failure(UNKNOWN_ERROR)
        }
}
