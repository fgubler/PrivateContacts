/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository

import ch.abwesend.privatecontacts.domain.lib.flow.ResourceFlow
import ch.abwesend.privatecontacts.domain.lib.flow.toResourceFlow
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.contact.ContactEditable
import ch.abwesend.privatecontacts.domain.model.contact.ContactWithPhoneNumbers
import ch.abwesend.privatecontacts.domain.model.contact.IContact
import ch.abwesend.privatecontacts.domain.model.contact.IContactBase
import ch.abwesend.privatecontacts.domain.model.contact.IContactIdInternal
import ch.abwesend.privatecontacts.domain.model.result.ContactChangeError.UNKNOWN_ERROR
import ch.abwesend.privatecontacts.domain.model.result.ContactDeleteResult
import ch.abwesend.privatecontacts.domain.model.result.ContactSaveResult
import ch.abwesend.privatecontacts.domain.model.search.ContactSearchConfig
import ch.abwesend.privatecontacts.domain.repository.IContactRepository
import ch.abwesend.privatecontacts.domain.repository.PAGING_DEPRECATION
import ch.abwesend.privatecontacts.domain.service.FullTextSearchService
import ch.abwesend.privatecontacts.domain.settings.Settings
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import ch.abwesend.privatecontacts.infrastructure.room.contact.toContactBase
import ch.abwesend.privatecontacts.infrastructure.room.contact.toEntity
import ch.abwesend.privatecontacts.infrastructure.room.database.AppDatabase
import kotlinx.coroutines.flow.map

class ContactRepository : RepositoryBase(), IContactRepository {
    private val contactDataRepository: ContactDataRepository by injectAnywhere()
    private val searchService: FullTextSearchService by injectAnywhere()

    override suspend fun getContactsAsFlow(searchConfig: ContactSearchConfig): ResourceFlow<List<IContactBase>> =
        withDatabase { database ->
            val dataFlow = when (searchConfig) {
                is ContactSearchConfig.All -> database.contactDao().getAllAsFlow()
                is ContactSearchConfig.Query -> {
                    val phoneNumberQuery = searchService.prepareQueryForPhoneNumberSearch(searchConfig.query)
                        .takeIf { searchService.isLongEnough(it) }.orEmpty()
                    database.contactDao().searchAsFlow(query = searchConfig.query, phoneNumberQuery = phoneNumberQuery)
                }
            }

            val resultFlow = dataFlow.map { entities ->
                entities.map { it.toContactBase() }
                    .also { logger.info("Loaded ${it.size} contacts") }
            }

            resultFlow.toResourceFlow()
        }

    @Deprecated(PAGING_DEPRECATION)
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

    override suspend fun findContactsWithNumberEndingOn(
        endOfPhoneNumber: String
    ): List<ContactWithPhoneNumbers> {
        val contactData = contactDataRepository.findPhoneNumbersEndingOn(endOfPhoneNumber)
        return withDatabase { database ->
            val contactIds = contactData.keys.map { it.uuid }
            val contacts = database.contactDao().findByIds(contactIds)

            contacts.map { contactEntity ->
                ContactWithPhoneNumbers(
                    contactBase = contactEntity.toContactBase(),
                    phoneNumbers = contactData[contactEntity.id].orEmpty()
                )
            }
        }
    }

    private suspend fun AppDatabase.getAllContactsPaged(loadSize: Int, offsetInRows: Int): List<IContactBase> =
        if (Settings.current.orderByFirstName) {
            contactDao().getPagedByFirstName(loadSize = loadSize, offsetInRows = offsetInRows)
        } else {
            contactDao().getPagedByLastName(loadSize = loadSize, offsetInRows = offsetInRows)
        }.map { it.toContactBase() }

    private suspend fun AppDatabase.searchContactsPaged(
        config: ContactSearchConfig.Query,
        loadSize: Int,
        offsetInRows: Int
    ): List<IContactBase> {
        val phoneNumberQuery = searchService.prepareQueryForPhoneNumberSearch(config.query)
            .takeIf { searchService.isLongEnough(it) }.orEmpty()

        return if (Settings.current.orderByFirstName) {
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
        }.map { it.toContactBase() }
    }

    override suspend fun resolveContact(contactId: IContactIdInternal): IContact {
        val contactEntity = withDatabase { database ->
            database.contactDao().findById(contactId.uuid)
        } ?: throw IllegalArgumentException("Contact $contactId not found in database")

        val contactData = contactDataRepository.loadContactData(contactId)
        val resolvedData = contactData.mapNotNull { contactDataRepository.tryResolveContactData(it) }

        return ContactEditable(
            id = contactEntity.id,
            firstName = contactEntity.firstName,
            lastName = contactEntity.lastName,
            nickname = contactEntity.nickname,
            type = contactEntity.type,
            notes = contactEntity.notes,
            contactDataSet = resolvedData.toMutableList(),
            isNew = false,
        )
    }

    override suspend fun createContact(contactId: IContactIdInternal, contact: IContact): ContactSaveResult =
        try {
            withDatabase { database ->
                database.contactDao().insert(contact.toEntity(contactId))
                contactDataRepository.createContactData(contactId, contact)
                ContactSaveResult.Success
            }
        } catch (e: Exception) {
            logger.error("Failed to create contact ${contact.id}", e)
            ContactSaveResult.Failure(UNKNOWN_ERROR)
        }

    override suspend fun updateContact(contactId: IContactIdInternal, contact: IContact): ContactSaveResult =
        try {
            withDatabase { database ->
                database.contactDao().update(contact.toEntity(contactId))
                contactDataRepository.updateContactData(contactId, contact)
                ContactSaveResult.Success
            }
        } catch (e: Exception) {
            logger.error("Failed to update contact ${contact.id}", e)
            ContactSaveResult.Failure(UNKNOWN_ERROR)
        }

    override suspend fun deleteContacts(contactIds: Collection<IContactIdInternal>): ContactDeleteResult {
        val results = bulkOperation(contactIds) { database, chunkedContactIds ->
            try {
                database.contactDao().delete(contactIds = chunkedContactIds.map { it.uuid })
                // ContactData should be deleted by cascade-delete
                ContactDeleteResult.Success
            } catch (e: Exception) {
                logger.error("Failed to delete ${contactIds.size} contacts", e)
                ContactDeleteResult.Failure(UNKNOWN_ERROR)
            }
        }

        return results.reduce { first, second -> first.combine(second) }.also {
            logger.debug("Deletion result of ${contactIds.size} contacts: $it")
        }
    }
}
