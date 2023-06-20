/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository.vcard.mapping.contactdata

import ch.abwesend.privatecontacts.domain.model.contactdata.ContactData
import ch.abwesend.privatecontacts.domain.model.contactdata.Website
import ezvcard.property.Url

fun Url.toContactData(sortOrder: Int): ContactData? = value?.let {
    Website.createEmpty(sortOrder)
        .changeType(type = toContactDataType())
        .changeValue(value = it)
}
