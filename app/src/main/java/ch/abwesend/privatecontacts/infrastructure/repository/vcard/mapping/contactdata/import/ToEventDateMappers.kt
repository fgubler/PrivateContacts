/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository.vcard.mapping.contactdata.import

import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType
import ch.abwesend.privatecontacts.domain.model.contactdata.EventDate
import ezvcard.property.DateOrTimeProperty
import java.time.LocalDate

fun DateOrTimeProperty.toContactData(type: ContactDataType, sortOrder: Int): EventDate? {
    val fullDate = date?.let {
        runCatching { LocalDate.from(it) }.getOrNull()
    }
    val partialDate = partialDate?.let {
        EventDate.createDate(
            day = it.date,
            month = it.month,
            year = it.year
        )
    }
    val date = fullDate ?: partialDate

    return date?.let {
        EventDate.createEmpty(sortOrder)
            .changeType(type = type)
            .changeValue(value = it)
    }
}
