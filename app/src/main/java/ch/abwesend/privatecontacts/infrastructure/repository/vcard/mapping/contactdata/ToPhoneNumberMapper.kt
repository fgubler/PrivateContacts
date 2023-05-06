/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository.vcard.mapping.contactdata

import ch.abwesend.privatecontacts.domain.model.contactdata.ContactData
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType
import ch.abwesend.privatecontacts.domain.model.contactdata.PhoneNumber
import ezvcard.property.Telephone

fun Telephone.toContactData(sortOrder: Int): ContactData? = text?.let {
    PhoneNumber.createEmpty(sortOrder)
        .changeType(getContactDataType())
        .changeValue(value = it)
}

private fun Telephone.getContactDataType(): ContactDataType =
    types.orEmpty().filterNotNull().map { it.toContactDataType() }.getByPriority()
