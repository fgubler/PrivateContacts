package ch.abwesend.privatecontacts.infrastructure.repository

import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.contact.Contact
import ch.abwesend.privatecontacts.domain.model.contact.ContactBase
import ch.abwesend.privatecontacts.domain.model.contact.ContactFull
import ch.abwesend.privatecontacts.domain.model.contact.toContactEditable
import ch.abwesend.privatecontacts.domain.model.contactdata.PhoneNumber
import ch.abwesend.privatecontacts.domain.model.result.ContactSaveResult
import ch.abwesend.privatecontacts.domain.model.result.SavingError.UNKNOWN_ERROR
import ch.abwesend.privatecontacts.domain.repository.IContactRepository
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import ch.abwesend.privatecontacts.infrastructure.room.contact.toEntity
import ch.abwesend.privatecontacts.infrastructure.room.contactdata.ContactDataEntity
import ch.abwesend.privatecontacts.infrastructure.room.contactdata.ContactDataType.PHONE_NUMBER
import ch.abwesend.privatecontacts.infrastructure.room.contactdata.toContactDataSubType

class ContactRepository : RepositoryBase(), IContactRepository {
    private val contactDataRepository: ContactDataRepository by injectAnywhere()

    override suspend fun loadContacts(): List<ContactBase> =
        withDatabase { database ->
            database.contactDao().getAll().also {
                logger.debug("Loaded ${it.size} contacts")
            }
        }

    override suspend fun resolveContact(contact: ContactBase): ContactFull {
        val contactData = contactDataRepository.loadContactData(contact)
        val phoneNumbers = contactData.mapNotNull { tryResolvePhoneNumber(it) }

        return contact.toContactEditable(
            phoneNumbers = phoneNumbers.toMutableList()
        )
    }

    private fun tryResolvePhoneNumber(contactData: ContactDataEntity): PhoneNumber? {
        if (contactData.type != PHONE_NUMBER) return null
        val numberType = contactData.subType.toContactDataSubType() ?: return null

        return PhoneNumber(
            id = contactData.id,
            type = numberType,
            isMain = contactData.isMain,
            sortOrder = contactData.sortOrder,
            value = contactData.value,
        )
    }

    override suspend fun createContact(contact: Contact): ContactSaveResult =
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

    override suspend fun updateContact(contact: Contact): ContactSaveResult =
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
