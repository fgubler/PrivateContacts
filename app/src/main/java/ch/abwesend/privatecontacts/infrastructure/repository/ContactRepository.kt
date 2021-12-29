package ch.abwesend.privatecontacts.infrastructure.repository

import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.contact.Contact
import ch.abwesend.privatecontacts.domain.model.contact.ContactBase
import ch.abwesend.privatecontacts.domain.model.contact.ContactFull
import ch.abwesend.privatecontacts.domain.model.contactdata.PhoneNumber
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

        return ContactFull(
            contactBase = contact,
            phoneNumbers = phoneNumbers,
        )
    }

    private fun tryResolvePhoneNumber(contactData: ContactDataEntity): PhoneNumber? {
        if (contactData.type != PHONE_NUMBER) return null
        val numberType = contactData.subType.toContactDataSubType() ?: return null

        return PhoneNumber(
            value = contactData.value,
            type = numberType,
            isMainNumber = contactData.isMain
        )
    }

    override suspend fun createContact(contact: Contact) =
        withDatabase { database ->
            database.contactDao().insert(contact.toEntity())
            contactDataRepository.createContactData(contact)
        }
}
