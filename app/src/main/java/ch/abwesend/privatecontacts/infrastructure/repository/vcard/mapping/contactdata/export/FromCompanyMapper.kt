/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository.vcard.mapping.contactdata.export

import ch.abwesend.privatecontacts.domain.model.contactdata.Company
import ch.abwesend.privatecontacts.infrastructure.service.AndroidContactCompanyMappingService
import ezvcard.parameter.RelatedType
import ezvcard.property.Related

fun Company.toVCardCompany(mappingService: AndroidContactCompanyMappingService): Related {
    val vCardData = Related()
    vCardData.text = value
    getContactDataType(mappingService)?.let { vCardType -> vCardData.types.add(vCardType) }
    return vCardData
}

private fun Company.getContactDataType(mappingService: AndroidContactCompanyMappingService): RelatedType? {
    val typeString = mappingService.encodeToPseudoRelationshipLabel(type)
    return getRelatedType(typeString)
}
