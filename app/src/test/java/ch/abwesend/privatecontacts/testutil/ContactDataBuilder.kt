package ch.abwesend.privatecontacts.testutil

import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataSubType
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataSubType.Mobile
import ch.abwesend.privatecontacts.domain.model.contactdata.PhoneNumber

fun somePhoneNumber(
    value: String = "1234",
    type: ContactDataSubType = Mobile,
    isMainNumber: Boolean = false,
): PhoneNumber = PhoneNumber(
    value = value,
    type = type,
    isMainNumber = isMainNumber,
)
