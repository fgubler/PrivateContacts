package ch.abwesend.privatecontacts.infrastructure.repository

import ch.abwesend.privatecontacts.domain.model.contact.Contact
import ch.abwesend.privatecontacts.domain.model.contact.ContactBase
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactData
import ch.abwesend.privatecontacts.domain.model.contactdata.PhoneNumber
import ch.abwesend.privatecontacts.infrastructure.room.contactdata.ContactDataEntity
import ch.abwesend.privatecontacts.infrastructure.room.contactdata.ContactDataType
import ch.abwesend.privatecontacts.infrastructure.room.contactdata.toContactDataSubType
import ch.abwesend.privatecontacts.infrastructure.room.contactdata.toEntity

class ContactDataRepository : RepositoryBase() {
    suspend fun loadContactData(contact: ContactBase): List<ContactDataEntity> =
        withDatabase { database ->
            database.contactDataDao().getDataForContact(contact.id)
        }

    suspend fun createContactData(contact: Contact) =
        withDatabase { database ->
            val contactData = contact.phoneNumbers.map { phoneNumber ->
                phoneNumber.toEntity(contact.id)
            }

            database.contactDataDao().insertAll(contactData)
        }

    suspend fun updateContactData(contact: Contact) =
        withDatabase { database ->
            val contactData = contact.allContactData.filter { !it.isEmpty }

            val newData = contactData.filter { it.isNew }.map { it.toEntity(contact.id) }
            val existingData = contactData.filter { !it.isNew }.map { it.toEntity(contact.id) }

            database.contactDataDao().insertAll(newData)
            database.contactDataDao().updateAll(existingData)
        }

    fun tryResolvePhoneNumber(contactData: ContactDataEntity): PhoneNumber? {
        if (contactData.type != ContactDataType.PHONE_NUMBER) return null
        val numberType = contactData.subType.toContactDataSubType()

        return PhoneNumber(
            id = contactData.id,
            type = numberType,
            isMain = contactData.isMain,
            sortOrder = contactData.sortOrder,
            value = contactData.valueRaw,
            isNew = false,
        )
    }

    private val Contact.allContactData: List<ContactData>
        get() = phoneNumbers // add additional types here
}
