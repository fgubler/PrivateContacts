package ch.abwesend.privatecontacts.infrastructure.repository

import ch.abwesend.privatecontacts.domain.model.contact.Contact
import ch.abwesend.privatecontacts.domain.model.contact.ContactBase
import ch.abwesend.privatecontacts.infrastructure.room.contactdata.ContactDataEntity
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
            val contactData = contact.phoneNumbers.map { phoneNumber ->
                phoneNumber.toEntity(contact.id)
            }

            database.contactDataDao().updateAll(contactData)
        }
}
