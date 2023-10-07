/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository.vcard.mapping.contactdata.import

import ch.abwesend.privatecontacts.domain.model.contactdata.Company
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactData
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType
import ch.abwesend.privatecontacts.infrastructure.service.AndroidContactCompanyMappingService
import ezvcard.property.Organization
import ezvcard.property.Related

fun Related.toCompany(sortOrder: Int, mappingService: AndroidContactCompanyMappingService): ContactData? =
    text?.let { value ->
        firstTypeOrNull?.let { typeRaw ->
            val type = mappingService.decodeFromPseudoRelationshipLabel(typeRaw)
            Company.createEmpty(sortOrder)
                .changeType(type)
                .changeValue(value)
        }
    }

fun Organization.toCompany(sortOrder: Int): ContactData? {
    // the values describe multiple layers in the same organization (like company - department - team)
    val organizationNames = values.orEmpty().filterNotNull()
    return if (organizationNames.isEmpty()) null
    else {
        val fullName = organizationNames.joinToString(" - ")
        Company.createEmpty(sortOrder)
            .changeType(getContactDataType())
            .changeValue(fullName)
    }
}

val Related.firstTypeOrNull: String?
    get() = types.orEmpty().firstOrNull()?.value

private fun Organization.getContactDataType(): ContactDataType = typeValueToContactDataType(type)
