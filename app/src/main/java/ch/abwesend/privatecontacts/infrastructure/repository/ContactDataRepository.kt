package ch.abwesend.privatecontacts.infrastructure.repository

import ch.abwesend.privatecontacts.domain.model.ModelStatus.CHANGED
import ch.abwesend.privatecontacts.domain.model.ModelStatus.DELETED
import ch.abwesend.privatecontacts.domain.model.ModelStatus.NEW
import ch.abwesend.privatecontacts.domain.model.ModelStatus.UNCHANGED
import ch.abwesend.privatecontacts.domain.model.contact.IContact
import ch.abwesend.privatecontacts.domain.model.contact.IContactBase
import ch.abwesend.privatecontacts.domain.model.contactdata.PhoneNumber
import ch.abwesend.privatecontacts.infrastructure.room.contactdata.ContactDataCategory
import ch.abwesend.privatecontacts.infrastructure.room.contactdata.ContactDataEntity
import ch.abwesend.privatecontacts.infrastructure.room.contactdata.toContactDataType
import ch.abwesend.privatecontacts.infrastructure.room.contactdata.toEntity

class ContactDataRepository : RepositoryBase() {
    suspend fun loadContactData(contact: IContactBase): List<ContactDataEntity> =
        withDatabase { database ->
            database.contactDataDao().getDataForContact(contact.id)
        }

    suspend fun createContactData(contact: IContact) =
        withDatabase { database ->
            val contactData = contact.contactDataSet.map { contactData ->
                contactData.toEntity(contact.id)
            }

            database.contactDataDao().insertAll(contactData)
        }

    suspend fun updateContactData(contact: IContact) =
        withDatabase { database ->
            val contactData = contact.contactDataSet.filter { !it.isEmpty }

            val newData = contactData
                .filter { it.modelStatus == NEW }
                .map { it.toEntity(contact.id) }
            val changedData = contactData
                .filter { it.modelStatus == CHANGED }
                .map { it.toEntity(contact.id) }
            val deletedData = contactData
                .filter { it.modelStatus == DELETED }
                .map { it.toEntity(contact.id) }

            database.contactDataDao().insertAll(newData)
            database.contactDataDao().updateAll(changedData)
            database.contactDataDao().deleteAll(deletedData)
        }

    fun tryResolvePhoneNumber(contactData: ContactDataEntity): PhoneNumber? {
        if (contactData.category != ContactDataCategory.PHONE_NUMBER) return null
        val numberType = contactData.type.toContactDataType()

        return PhoneNumber(
            id = contactData.id,
            type = numberType,
            sortOrder = contactData.sortOrder,
            value = contactData.valueRaw,
            isMain = contactData.isMain,
            modelStatus = UNCHANGED,
        )
    }
}
