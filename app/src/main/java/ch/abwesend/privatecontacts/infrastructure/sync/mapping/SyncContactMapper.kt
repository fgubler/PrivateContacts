/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.sync.mapping

import ch.abwesend.privatecontacts.domain.model.ModelStatus
import ch.abwesend.privatecontacts.domain.model.contact.ContactAccount
import ch.abwesend.privatecontacts.domain.model.contact.ContactEditable
import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.model.contact.IContact
import ch.abwesend.privatecontacts.domain.model.contact.IContactEditable
import ch.abwesend.privatecontacts.domain.model.contact.IContactIdInternal
import ch.abwesend.privatecontacts.domain.model.contactdata.Company
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactData
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataCategory
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType
import ch.abwesend.privatecontacts.domain.model.contactdata.EmailAddress
import ch.abwesend.privatecontacts.domain.model.contactdata.EventDate
import ch.abwesend.privatecontacts.domain.model.contactdata.PhoneNumber
import ch.abwesend.privatecontacts.domain.model.contactdata.PhysicalAddress
import ch.abwesend.privatecontacts.domain.model.contactdata.Relationship
import ch.abwesend.privatecontacts.domain.model.contactdata.StringBasedContactData
import ch.abwesend.privatecontacts.domain.model.contactdata.Website
import ch.abwesend.privatecontacts.domain.model.contactdata.createContactDataId
import ch.abwesend.privatecontacts.domain.model.contactgroup.ContactGroup
import ch.abwesend.privatecontacts.domain.model.contactgroup.IContactGroup
import ch.abwesend.privatecontacts.domain.model.contactimage.ContactImage
import ch.abwesend.privatecontacts.domain.model.sync.SyncContact
import ch.abwesend.privatecontacts.domain.model.sync.SyncContactData
import ch.abwesend.privatecontacts.domain.model.sync.SyncContactImage
import java.util.Base64

/**
 * Maps between the internal [IContact] and the serializable [SyncContact] transport-DTO.
 *
 * Remote-applied contacts are always produced with [ModelStatus.NEW] so that the create-path
 * (delete-then-create in the pull-worker) persists the whole object (contact-data, groups, image).
 */
class SyncContactMapper {
    fun toSyncContact(contact: IContact, syncId: String): SyncContact =
        SyncContact(
            syncId = syncId,
            firstName = contact.firstName,
            lastName = contact.lastName,
            nickname = contact.nickname,
            middleName = contact.middleName,
            namePrefix = contact.namePrefix,
            nameSuffix = contact.nameSuffix,
            notes = contact.notes,
            contactData = contact.contactDataSet
                .filterNot { it.isEmpty }
                .map { it.toSyncContactData() },
            groups = contact.contactGroups.map { it.id.name },
            image = contact.image.toSyncContactImage(),
        )

    fun toContact(syncContact: SyncContact, contactId: IContactIdInternal): IContactEditable =
        ContactEditable(
            id = contactId,
            importId = null,
            firstName = syncContact.firstName,
            lastName = syncContact.lastName,
            nickname = syncContact.nickname,
            middleName = syncContact.middleName,
            namePrefix = syncContact.namePrefix,
            nameSuffix = syncContact.nameSuffix,
            type = ContactType.SECRET,
            notes = syncContact.notes,
            image = syncContact.image.toContactImage(),
            contactDataSet = syncContact.contactData.map { it.toContactData() }.toMutableList(),
            contactGroups = syncContact.groups
                .mapTo(mutableListOf<IContactGroup>()) { ContactGroup.new(it) },
            saveInAccount = ContactAccount.None,
            isNew = true,
        )

    private fun ContactData.toSyncContactData(): SyncContactData {
        val serializedValue = when (this) {
            is EventDate -> serializedValue()
            is StringBasedContactData -> value
            else -> value?.toString().orEmpty()
        }
        val customValue = (type as? ContactDataType.CustomValue)?.customValue
        return SyncContactData(
            category = category,
            typeKey = type.key.name,
            typeCustomValue = customValue,
            value = serializedValue,
            sortOrder = sortOrder,
            isMain = isMain,
        )
    }

    private fun SyncContactData.toContactData(): ContactData {
        val typeKey = ContactDataType.Key.parseOrNull(typeKey) ?: ContactDataType.Key.OTHER
        val contactDataType = ContactDataType.fromKey(key = typeKey, customValue = typeCustomValue)
        val dataId = createContactDataId()

        return when (category) {
            ContactDataCategory.PHONE_NUMBER -> PhoneNumber(
                id = dataId,
                sortOrder = sortOrder,
                type = contactDataType,
                value = value,
                isMain = isMain,
                modelStatus = ModelStatus.NEW,
            )
            ContactDataCategory.EMAIL -> EmailAddress(
                id = dataId,
                sortOrder = sortOrder,
                type = contactDataType,
                value = value,
                isMain = isMain,
                modelStatus = ModelStatus.NEW,
            )
            ContactDataCategory.ADDRESS -> PhysicalAddress(
                id = dataId,
                sortOrder = sortOrder,
                type = contactDataType,
                value = value,
                isMain = isMain,
                modelStatus = ModelStatus.NEW,
            )
            ContactDataCategory.WEBSITE -> Website(
                id = dataId,
                sortOrder = sortOrder,
                type = contactDataType,
                value = value,
                isMain = isMain,
                modelStatus = ModelStatus.NEW,
            )
            ContactDataCategory.COMPANY -> Company(
                id = dataId,
                sortOrder = sortOrder,
                type = contactDataType,
                value = value,
                isMain = isMain,
                modelStatus = ModelStatus.NEW,
            )
            ContactDataCategory.RELATIONSHIP -> Relationship(
                id = dataId,
                sortOrder = sortOrder,
                type = contactDataType,
                value = value,
                isMain = isMain,
                modelStatus = ModelStatus.NEW,
            )
            ContactDataCategory.EVENT_DATE -> EventDate(
                id = dataId,
                sortOrder = sortOrder,
                type = contactDataType,
                value = EventDate.deserializeDate(value),
                isMain = isMain,
                modelStatus = ModelStatus.NEW,
            )
        }
    }

    private fun ContactImage.toSyncContactImage(): SyncContactImage? =
        if (isEmpty) null
        else SyncContactImage(
            thumbnailUri = thumbnailUri,
            fullImageBase64 = fullImage?.let { Base64.getEncoder().encodeToString(it) },
        )

    private fun SyncContactImage?.toContactImage(): ContactImage =
        if (this == null) ContactImage.empty
        else ContactImage(
            thumbnailUri = thumbnailUri,
            fullImage = fullImageBase64?.let { Base64.getDecoder().decode(it) },
            modelStatus = ModelStatus.NEW,
        )
}
