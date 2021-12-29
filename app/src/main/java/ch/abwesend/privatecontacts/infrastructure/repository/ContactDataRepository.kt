package ch.abwesend.privatecontacts.infrastructure.repository

import ch.abwesend.privatecontacts.domain.model.contact.Contact
import ch.abwesend.privatecontacts.domain.model.contact.ContactBase
import ch.abwesend.privatecontacts.infrastructure.room.contactdata.ContactDataEntity
import ch.abwesend.privatecontacts.infrastructure.room.contactdata.ContactDataType
import ch.abwesend.privatecontacts.infrastructure.room.contactdata.toEntity
import java.util.UUID

class ContactDataRepository : RepositoryBase() {
    suspend fun loadContactData(contact: ContactBase): List<ContactDataEntity> =
        withDatabase { database ->
            database.contactDataDao().getDataForContact(contact.id)
        }

    suspend fun createContactData(contact: Contact) =
        withDatabase { database ->
            val contactData = contact.phoneNumbers.map { phoneNumber ->
                ContactDataEntity(
                    id = UUID.randomUUID(),
                    contactId = contact.id,
                    type = ContactDataType.PHONE_NUMBER,
                    subType = phoneNumber.type.toEntity(),
                    isMain = phoneNumber.isMainNumber,
                    value = phoneNumber.value
                )
            }

            database.contactDataDao().insertAll(contactData)
        }
}
