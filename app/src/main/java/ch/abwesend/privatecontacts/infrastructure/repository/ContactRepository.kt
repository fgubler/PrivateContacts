/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository

import ch.abwesend.privatecontacts.domain.Settings
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.contact.IContact
import ch.abwesend.privatecontacts.domain.model.contact.IContactBase
import ch.abwesend.privatecontacts.domain.model.contact.toContactEditable
import ch.abwesend.privatecontacts.domain.model.result.ContactSaveResult
import ch.abwesend.privatecontacts.domain.model.result.ContactSavingError.UNKNOWN_ERROR
import ch.abwesend.privatecontacts.domain.model.search.ContactSearchConfig
import ch.abwesend.privatecontacts.domain.repository.IContactRepository
import ch.abwesend.privatecontacts.domain.service.FullTextSearchService
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

    private suspend fun AppDatabase.getAllContactsPaged(loadSize: Int, offsetInRows: Int): List<IContactBase> =
        if (Settings.orderByFirstName) {
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

        return if (Settings.orderByFirstName) {
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
        val contactData = contactDataRepository.loadContactData(contact)
        val resolvedData = contactData.mapNotNull { contactDataRepository.tryResolveContactData(it) }

        return contact.toContactEditable(contactDataSet = resolvedData.toMutableList())
    }

    override suspend fun createContact(contact: IContact): ContactSaveResult =
        try {
            withDatabase { database ->
                database.contactDao().insert(contact.toEntity())
                contactDataRepository.createContactData(contact)
                ContactSaveResult.Success
            }
        } catch (e: Exception) {
            logger.error("Failed to create contact", e)
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
            logger.error("Failed to update contact", e)
            ContactSaveResult.Failure(UNKNOWN_ERROR)
        }
}
