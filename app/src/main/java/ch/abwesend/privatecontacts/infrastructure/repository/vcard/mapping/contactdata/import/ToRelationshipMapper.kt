/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository.vcard.mapping.contactdata.import

import ch.abwesend.privatecontacts.domain.model.contactdata.Company
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactData
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType
import ch.abwesend.privatecontacts.domain.model.contactdata.Relationship
import ch.abwesend.privatecontacts.infrastructure.service.AndroidContactCompanyMappingService
import ezvcard.property.Related

fun Related.toRelationship(sortOrder: Int): ContactData? = text?.let { value ->
    Relationship.createEmpty(sortOrder)
        .changeType(getContactDataType())
        .changeValue(value)
}

fun Related.toCompany(sortOrder: Int, mappingService: AndroidContactCompanyMappingService): ContactData? =
    text?.let { value ->
        firstTypeOrNull?.let { typeRaw ->
            val type = mappingService.decodeFromPseudoRelationshipLabel(typeRaw)
            Company.createEmpty(sortOrder)
                .changeType(type)
                .changeValue(value)
        }
    }

val Related.firstTypeOrNull: String?
    get() = types.orEmpty().firstOrNull()?.value

private fun Related.getContactDataType(): ContactDataType =
    types.orEmpty().filterNotNull().map { it.toContactDataType() }.getByPriority()
