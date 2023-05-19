/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.testutil.databuilders

import ch.abwesend.privatecontacts.domain.model.ModelStatus
import ch.abwesend.privatecontacts.domain.model.contactdata.Company
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactData
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataCategory
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataCategory.PHONE_NUMBER
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataId
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataIdAndroid
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.Key.PERSONAL
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.Mobile
import ch.abwesend.privatecontacts.domain.model.contactdata.EmailAddress
import ch.abwesend.privatecontacts.domain.model.contactdata.EventDate
import ch.abwesend.privatecontacts.domain.model.contactdata.PhoneNumber
import ch.abwesend.privatecontacts.domain.model.contactdata.PhysicalAddress
import ch.abwesend.privatecontacts.domain.model.contactdata.Relationship
import ch.abwesend.privatecontacts.domain.model.contactdata.Website
import ch.abwesend.privatecontacts.domain.model.contactdata.createContactDataId
import ch.abwesend.privatecontacts.infrastructure.room.contactdata.ContactDataEntity
import ch.abwesend.privatecontacts.infrastructure.room.contactdata.ContactDataTypeEntity
import java.time.LocalDate
import java.util.UUID

fun someContactDataId(): ContactDataId = createContactDataId()
fun someContactDataIdExternal(contactDataNo: Int = 42): ContactDataId =
    ContactDataIdAndroid(contactDataNo = contactDataNo.toLong())

fun someContactDataEntity(
    id: UUID = UUID.randomUUID(),
    contactId: UUID = UUID.randomUUID(),
    value: String = "1234",
    category: ContactDataCategory = PHONE_NUMBER,
    type: ContactDataTypeEntity = someContactDataTypeEntity(),
    sortOrder: Int = 0,
    isMain: Boolean = false,
): ContactDataEntity = ContactDataEntity(
    id = id,
    contactId = contactId,
    valueRaw = value,
    valueFormatted = value,
    valueForMatching = value,
    category = category,
    type = type,
    isMain = isMain,
    sortOrder = sortOrder,
)

fun someContactDataTypeEntity(
    key: ContactDataType.Key = PERSONAL,
    customValue: String? = null
): ContactDataTypeEntity =
    ContactDataTypeEntity(
        key = key,
        customValue = customValue
    )

fun somePhoneNumber(
    id: ContactDataId = someContactDataId(),
    value: String = "1234",
    formattedValue: String = value,
    type: ContactDataType = Mobile,
    sortOrder: Int = 0,
    isMainNumber: Boolean = false,
    modelStatus: ModelStatus = ModelStatus.CHANGED,
): PhoneNumber = PhoneNumber(
    id = id,
    value = value,
    formattedValue = formattedValue,
    type = type,
    isMain = isMainNumber,
    modelStatus = modelStatus,
    sortOrder = sortOrder,
)

fun someEmailAddress(
    id: ContactDataId = someContactDataId(),
    value: String = "luke@jedi.com",
    type: ContactDataType = ContactDataType.Personal,
    sortOrder: Int = 0,
    isMain: Boolean = false,
    modelStatus: ModelStatus = ModelStatus.CHANGED,
): EmailAddress = EmailAddress(
    id = id,
    value = value,
    type = type,
    isMain = isMain,
    modelStatus = modelStatus,
    sortOrder = sortOrder,
)

fun somePhysicalAddress(
    id: ContactDataId = someContactDataId(),
    value: String = "Jedi-Street 134",
    type: ContactDataType = ContactDataType.Personal,
    sortOrder: Int = 0,
    isMain: Boolean = false,
    modelStatus: ModelStatus = ModelStatus.CHANGED,
): PhysicalAddress = PhysicalAddress(
    id = id,
    value = value,
    type = type,
    isMain = isMain,
    modelStatus = modelStatus,
    sortOrder = sortOrder,
)

fun someEventDate(
    id: ContactDataId = someContactDataId(),
    value: LocalDate = LocalDate.now(),
    type: ContactDataType = ContactDataType.Birthday,
    sortOrder: Int = 0,
    isMain: Boolean = false,
    modelStatus: ModelStatus = ModelStatus.CHANGED,
): EventDate = EventDate(
    id = id,
    value = value,
    type = type,
    isMain = isMain,
    modelStatus = modelStatus,
    sortOrder = sortOrder,
)

@Suppress("DeprecatedCallableAddReplaceWith")
@Deprecated("URLs don't work in Unit-Tests, so the Websites are lost...")
fun someWebsite(
    id: ContactDataId = someContactDataId(),
    value: String = "www.private-contacts.com",
    type: ContactDataType = ContactDataType.Personal,
    sortOrder: Int = 0,
    isMain: Boolean = false,
    modelStatus: ModelStatus = ModelStatus.CHANGED,
): Website = Website(
    id = id,
    value = value,
    type = type,
    isMain = isMain,
    modelStatus = modelStatus,
    sortOrder = sortOrder,
)

fun someRelationship(
    id: ContactDataId = someContactDataId(),
    value: String = "Darth Vader",
    type: ContactDataType = ContactDataType.RelationshipBrother,
    sortOrder: Int = 0,
    isMain: Boolean = false,
    modelStatus: ModelStatus = ModelStatus.CHANGED,
): Relationship = Relationship(
    id = id,
    value = value,
    type = type,
    isMain = isMain,
    modelStatus = modelStatus,
    sortOrder = sortOrder,
)

fun someCompany(
    id: ContactDataId = someContactDataId(),
    value: String = "Sith Inc.",
    type: ContactDataType = ContactDataType.Main,
    sortOrder: Int = 0,
    isMain: Boolean = false,
    modelStatus: ModelStatus = ModelStatus.CHANGED,
): Company = Company(
    id = id,
    value = value,
    type = type,
    isMain = isMain,
    modelStatus = modelStatus,
    sortOrder = sortOrder,
)

fun someListOfContactData(
    modelStatus: ModelStatus = ModelStatus.NEW,
    internalIds: Boolean = true,
): List<ContactData> =
    listOf(
        somePhoneNumber(modelStatus = modelStatus, id = someContactDataId(internalIds)),
        someEmailAddress(modelStatus = modelStatus, id = someContactDataId(internalIds)),
        somePhysicalAddress(modelStatus = modelStatus, id = someContactDataId(internalIds)),
        someEventDate(modelStatus = modelStatus, id = someContactDataId(internalIds)),
        someRelationship(modelStatus = modelStatus, id = someContactDataId(internalIds)),
        someCompany(modelStatus = modelStatus, id = someContactDataId(internalIds)),
        // no websites: they don't work in unit-tests
    )

private fun someContactDataId(internalId: Boolean): ContactDataId =
    if (internalId) someContactDataId() else someContactDataIdExternal()

fun someListOfExternalContactData(modelStatus: ModelStatus = ModelStatus.NEW): List<ContactData> =
    listOf(
        somePhoneNumber(id = someContactDataIdExternal(123), modelStatus = modelStatus),
        someEmailAddress(id = someContactDataIdExternal(234), modelStatus = modelStatus),
        somePhysicalAddress(id = someContactDataIdExternal(345), modelStatus = modelStatus),
        someEventDate(id = someContactDataIdExternal(456), modelStatus = modelStatus),
        someRelationship(id = someContactDataIdExternal(567), modelStatus = modelStatus),
        // no websites: they don't work in unit-tests
    )
