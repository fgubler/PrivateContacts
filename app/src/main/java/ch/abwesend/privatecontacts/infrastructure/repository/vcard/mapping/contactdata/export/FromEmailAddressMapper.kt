/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository.vcard.mapping.contactdata.export

import ch.abwesend.privatecontacts.domain.model.contactdata.EmailAddress
import ch.abwesend.privatecontacts.infrastructure.repository.vcard.mapping.contactdata.getVCardStringByContactDataType
import ezvcard.parameter.EmailType
import ezvcard.property.Email

fun EmailAddress.toVCardEmailAddress(): Email {
    val vCardData = Email(value)
    val vCardType = getContactDataType()
    vCardData.types.add(vCardType)
    return vCardData
}

private fun EmailAddress.getContactDataType(): EmailType {
    val typeString = getVCardStringByContactDataType(type)
    return EmailType.get(typeString)
}
