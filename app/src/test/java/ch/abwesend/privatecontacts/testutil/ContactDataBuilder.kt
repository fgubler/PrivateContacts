package ch.abwesend.privatecontacts.testutil

import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataSubType
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataSubType.Key.PRIVATE
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataSubType.Mobile
import ch.abwesend.privatecontacts.domain.model.contactdata.PhoneNumber
import ch.abwesend.privatecontacts.infrastructure.room.contactdata.ContactDataEntity
import ch.abwesend.privatecontacts.infrastructure.room.contactdata.ContactDataSubTypeEntity
import ch.abwesend.privatecontacts.infrastructure.room.contactdata.ContactDataType
import ch.abwesend.privatecontacts.infrastructure.room.contactdata.ContactDataType.PHONE_NUMBER
import java.util.UUID

fun someContactDataEntity(
    id: UUID = UUID.randomUUID(),
    contactId: UUID = UUID.randomUUID(),
    value: String = "1234",
    type: ContactDataType = PHONE_NUMBER,
    subType: ContactDataSubTypeEntity = someContactDataSubTypeEntity(),
    sortOrder: Int? = null,
    isMain: Boolean = false,
): ContactDataEntity = ContactDataEntity(
    id = id,
    contactId = contactId,
    value = value,
    type = type,
    subType = subType,
    isMain = isMain,
    sortOrder = sortOrder,
)

fun someContactDataSubTypeEntity(
    key: ContactDataSubType.Key = PRIVATE,
    customValue: String? = null
): ContactDataSubTypeEntity =
    ContactDataSubTypeEntity(
        key = key,
        customValue = customValue
    )

fun somePhoneNumber(
    id: UUID = UUID.randomUUID(),
    value: String = "1234",
    type: ContactDataSubType = Mobile,
    sortOrder: Int? = null,
    isMainNumber: Boolean = false,
): PhoneNumber = PhoneNumber(
    id = id,
    value = value,
    type = type,
    isMain = isMainNumber,
    sortOrder = sortOrder,
)
