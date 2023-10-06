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

fun Organization.toCompany(sortOrder: Int): List<ContactData> =
    values.orEmpty().mapIndexed { index, companyName ->
        val contactDataType = getContactDataType()
        Company.createEmpty(sortOrder = sortOrder + index)
            .changeType(contactDataType)
            .changeValue(companyName)
    }

val Related.firstTypeOrNull: String?
    get() = types.orEmpty().firstOrNull()?.value

private fun Organization.getContactDataType(): ContactDataType = typeValueToContactDataType(type)
