/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository.vcard.mapping.contactdata.export

import ch.abwesend.privatecontacts.domain.model.contactdata.EventDate
import ezvcard.property.Anniversary
import ezvcard.property.Birthday
import ezvcard.util.PartialDate
import java.time.LocalDate

fun EventDate.toVCardBirthday(): Birthday? = value?.let { date ->
    if (isYearSet) Birthday(value) else Birthday(date.toPartialDate())
}

fun EventDate.toVCardAnniversary(): Anniversary? = value?.let { date ->
    if (isYearSet) Anniversary(value) else Anniversary(date.toPartialDate())
}

private fun LocalDate.toPartialDate(): PartialDate =
    PartialDate.builder().date(dayOfMonth).month(monthValue).build()
