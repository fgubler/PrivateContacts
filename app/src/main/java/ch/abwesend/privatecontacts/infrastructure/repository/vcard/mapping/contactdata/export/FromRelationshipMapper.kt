/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository.vcard.mapping.contactdata.export

import ch.abwesend.privatecontacts.domain.model.contactdata.Relationship
import ch.abwesend.privatecontacts.infrastructure.repository.vcard.mapping.contactdata.getVCardStringByContactDataType
import ezvcard.parameter.RelatedType
import ezvcard.property.Related

fun Relationship.toVCardRelationship(): Related {
    val vCardData = Related()
    vCardData.text = value
    getContactDataType()?.let { vCardType -> vCardData.types.add(vCardType) }
    return vCardData
}

private fun Relationship.getContactDataType(): RelatedType? {
    val typeString = getVCardStringByContactDataType(type)
    return getRelatedType(typeString)
}
