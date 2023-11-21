/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository.vcard.mapping.contactdata.export

import ch.abwesend.privatecontacts.domain.model.contactdata.Website
import ch.abwesend.privatecontacts.infrastructure.repository.vcard.mapping.contactdata.getVCardStringByContactDataType
import ezvcard.property.Url

fun Website.toVCardUrl(): Url {
    val vCardData = Url(value)
    val vCardType = getContactDataType()
    vCardData.type = vCardType
    return vCardData
}

private fun Website.getContactDataType(): String = getVCardStringByContactDataType(type)
