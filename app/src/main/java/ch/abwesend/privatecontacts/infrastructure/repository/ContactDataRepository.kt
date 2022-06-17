/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository

import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.ModelStatus.CHANGED
import ch.abwesend.privatecontacts.domain.model.ModelStatus.DELETED
import ch.abwesend.privatecontacts.domain.model.ModelStatus.NEW
import ch.abwesend.privatecontacts.domain.model.ModelStatus.UNCHANGED
import ch.abwesend.privatecontacts.domain.model.contact.ContactDataIdInternal
import ch.abwesend.privatecontacts.domain.model.contact.ContactIdInternal
import ch.abwesend.privatecontacts.domain.model.contact.IContact
import ch.abwesend.privatecontacts.domain.model.contact.IContactIdInternal
import ch.abwesend.privatecontacts.domain.model.contactdata.Company
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactData
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataCategory
import ch.abwesend.privatecontacts.domain.model.contactdata.EmailAddress
import ch.abwesend.privatecontacts.domain.model.contactdata.PhoneNumber
import ch.abwesend.privatecontacts.domain.model.contactdata.PhoneNumberValue
import ch.abwesend.privatecontacts.domain.model.contactdata.PhysicalAddress
import ch.abwesend.privatecontacts.domain.model.contactdata.Website
import ch.abwesend.privatecontacts.infrastructure.room.contactdata.ContactDataEntity
import ch.abwesend.privatecontacts.infrastructure.room.contactdata.toContactDataType
import ch.abwesend.privatecontacts.infrastructure.room.contactdata.toEntity

class ContactDataRepository : RepositoryBase() {
    suspend fun loadContactData(contactId: IContactIdInternal): List<ContactDataEntity> =
        withDatabase { database ->
            database.contactDataDao().getDataForContact(contactId.uuid)
        }

    suspend fun findPhoneNumbersEndingOn(endOfPhoneNumber: String): Map<ContactIdInternal, List<PhoneNumberValue>> =
        withDatabase { database ->
            val data = database.contactDataDao().findPhoneNumbersEndingOn(endOfPhoneNumber)
            data
                .groupBy { it.contactId }
                .mapKeys { ContactIdInternal(it.key) }
                .mapValues { pair -> pair.value.map { PhoneNumberValue(it.valueRaw) } }
        }

    suspend fun createContactData(contactId: IContactIdInternal, contact: IContact) =
        withDatabase { database ->
            val contactData = contact.contactDataSet.map { contactData ->
                contactData.toEntity(contactId)
            }

            database.contactDataDao().insertAll(contactData)
        }

    suspend fun updateContactData(contactId: IContactIdInternal, contact: IContact) =
        withDatabase { database ->
            val contactData = contact.contactDataSet.filter { !it.isEmpty }

            val newData = contactData
                .filter { it.modelStatus == NEW }
                .map { it.toEntity(contactId) }
            val changedData = contactData
                .filter { it.modelStatus == CHANGED }
                .map { it.toEntity(contactId) }
            val deletedData = contactData
                .filter { it.modelStatus == DELETED }
                .map { it.toEntity(contactId) }

            database.contactDataDao().insertAll(newData)
            database.contactDataDao().updateAll(changedData)
            database.contactDataDao().deleteAll(deletedData)
        }

    fun tryResolveContactData(contactData: ContactDataEntity): ContactData? = try {
        val type = contactData.type.toContactDataType()

        when (contactData.category) {
            ContactDataCategory.PHONE_NUMBER -> PhoneNumber(
                id = ContactDataIdInternal(contactData.id),
                type = type,
                sortOrder = contactData.sortOrder,
                value = contactData.valueRaw,
                formattedValue = contactData.valueFormatted,
                isMain = contactData.isMain,
                modelStatus = UNCHANGED,
            )
            ContactDataCategory.EMAIL -> EmailAddress(
                id = ContactDataIdInternal(contactData.id),
                type = type,
                sortOrder = contactData.sortOrder,
                value = contactData.valueRaw,
                isMain = contactData.isMain,
                modelStatus = UNCHANGED,
            )
            ContactDataCategory.ADDRESS -> PhysicalAddress(
                id = ContactDataIdInternal(contactData.id),
                type = type,
                sortOrder = contactData.sortOrder,
                value = contactData.valueRaw,
                isMain = contactData.isMain,
                modelStatus = UNCHANGED,
            )
            ContactDataCategory.WEBSITE -> Website(
                id = ContactDataIdInternal(contactData.id),
                type = type,
                sortOrder = contactData.sortOrder,
                value = contactData.valueRaw,
                isMain = contactData.isMain,
                modelStatus = UNCHANGED,
            )
            ContactDataCategory.COMPANY -> Company(
                id = ContactDataIdInternal(contactData.id),
                type = type,
                sortOrder = contactData.sortOrder,
                value = contactData.valueRaw,
                isMain = contactData.isMain,
                modelStatus = UNCHANGED,
            )

            ContactDataCategory.DATE -> null // TODO implement
        }
    } catch (e: Exception) {
        logger.error("Failed to resolve contact-data", e)
        null
    }

    suspend fun deleteContactData(contactId: IContactIdInternal) = deleteContactData(listOf(contactId))

    suspend fun deleteContactData(contactIds: Collection<IContactIdInternal>) = withDatabase { database ->
        val dataToDelete = database.contactDataDao().getDataForContacts(contactIds.map { it.uuid })
        database.contactDataDao().deleteAll(dataToDelete)
    }
}
