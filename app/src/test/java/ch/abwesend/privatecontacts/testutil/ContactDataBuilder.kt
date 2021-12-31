package ch.abwesend.privatecontacts.testutil

import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataSubType
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataSubType.Mobile
import ch.abwesend.privatecontacts.domain.model.contactdata.PhoneNumber
import java.util.UUID

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
