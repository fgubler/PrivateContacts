/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository.vcard.mapping.contactdata.export

import ch.abwesend.privatecontacts.domain.model.contactdata.PhoneNumber
import ch.abwesend.privatecontacts.infrastructure.repository.vcard.mapping.contactdata.getVCardStringByContactDataType
import ezvcard.parameter.TelephoneType
import ezvcard.property.Telephone

fun PhoneNumber.toVCardPhoneNumber(): Telephone {
    val vCardData = Telephone(value)
    val vCardType = getContactDataType()
    vCardData.types.add(vCardType)
    return vCardData
}

private fun PhoneNumber.getContactDataType(): TelephoneType {
    val typeString = getVCardStringByContactDataType(type)
    return TelephoneType.get(typeString)
}
