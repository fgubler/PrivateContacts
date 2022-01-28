package ch.abwesend.privatecontacts.infrastructure.repository

import ch.abwesend.privatecontacts.domain.Settings
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.contact.IContact
import ch.abwesend.privatecontacts.domain.model.contact.IContactBase
import ch.abwesend.privatecontacts.domain.model.contact.toContactEditable
import ch.abwesend.privatecontacts.domain.model.result.ContactSaveResult
import ch.abwesend.privatecontacts.domain.model.result.ContactSavingError.UNKNOWN_ERROR
import ch.abwesend.privatecontacts.domain.repository.IContactRepository
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import ch.abwesend.privatecontacts.infrastructure.room.contact.toEntity

class ContactRepository : RepositoryBase(), IContactRepository {
    private val contactDataRepository: ContactDataRepository by injectAnywhere()

    override suspend fun loadContacts(): List<IContactBase> =
        withDatabase { database ->
            database.contactDao().getAll().also {
                logger.info("Loaded ${it.size} contacts")
            }
        }

    override suspend fun getContactsPaged(loadSize: Int, offsetInRows: Int): List<IContactBase> =
        withDatabase { database ->
            logger.info("Loading contacts with pageSize = $loadSize and offset = $offsetInRows")

            val result = if (Settings.orderByFirstName) {
                database.contactDao().getPagedByFirstName(loadSize = loadSize, offsetInRows = offsetInRows)
            } else {
                database.contactDao().getPagedByLastName(loadSize = loadSize, offsetInRows = offsetInRows)
            }

            logger.info("Loaded ${result.size} contacts with pageSize = $loadSize and offset = $offsetInRows")
            result
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
