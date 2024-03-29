/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository.vcard.mapping.contactdata.import

import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType
import ch.abwesend.privatecontacts.domain.model.contactdata.EmailAddress
import ezvcard.property.Email

fun Email.toContactData(sortOrder: Int): EmailAddress? = value?.let {
    EmailAddress.createEmpty(sortOrder)
        .changeType(getContactDataType())
        .changeValue(value = it)
}

private fun Email.getContactDataType(): ContactDataType =
    types.orEmpty().filterNotNull().map { it.toContactDataType() }.getByPriority()
