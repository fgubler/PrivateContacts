package ch.abwesend.privatecontacts.testutil

import ch.abwesend.privatecontacts.domain.model.ModelStatus
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataCategory
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataCategory.PHONE_NUMBER
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataId
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.Key.PRIVATE
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.Mobile
import ch.abwesend.privatecontacts.domain.model.contactdata.EmailAddress
import ch.abwesend.privatecontacts.domain.model.contactdata.PhoneNumber
import ch.abwesend.privatecontacts.infrastructure.room.contactdata.ContactDataEntity
import ch.abwesend.privatecontacts.infrastructure.room.contactdata.ContactDataTypeEntity
import java.util.UUID

fun someContactDataId(): ContactDataId = ContactDataId.randomId()

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
    category = category,
    type = type,
    isMain = isMain,
    sortOrder = sortOrder,
)

fun someContactDataTypeEntity(
    key: ContactDataType.Key = PRIVATE,
    customValue: String? = null
): ContactDataTypeEntity =
    ContactDataTypeEntity(
        key = key,
        customValue = customValue
    )

fun somePhoneNumber(
    id: ContactDataId = someContactDataId(),
    value: String = "1234",
    type: ContactDataType = Mobile,
    sortOrder: Int = 0,
    isMainNumber: Boolean = false,
    modelStatus: ModelStatus = ModelStatus.CHANGED,
): PhoneNumber = PhoneNumber(
    id = id,
    value = value,
    type = type,
    isMain = isMainNumber,
    modelStatus = modelStatus,
    sortOrder = sortOrder,
)

fun someEmailAddress(
    id: ContactDataId = someContactDataId(),
    value: String = "1234",
    type: ContactDataType = ContactDataType.Private,
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
