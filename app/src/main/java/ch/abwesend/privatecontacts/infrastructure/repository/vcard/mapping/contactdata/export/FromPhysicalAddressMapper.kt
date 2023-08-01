/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository.vcard.mapping.contactdata.export

import ch.abwesend.privatecontacts.domain.model.contactdata.PhysicalAddress
import ch.abwesend.privatecontacts.infrastructure.repository.vcard.mapping.contactdata.getVCardStringByContactDataType
import ezvcard.parameter.AddressType
import ezvcard.property.Address

fun PhysicalAddress.toVCardAddress(): Address {
    val vCardData = Address()
    vCardData.streetAddress = value
    val vCardType = getContactDataType()
    vCardData.types.add(vCardType)
    return vCardData
}

private fun PhysicalAddress.getContactDataType(): AddressType {
    val typeString = getVCardStringByContactDataType(type)
    return AddressType.get(typeString)
}
