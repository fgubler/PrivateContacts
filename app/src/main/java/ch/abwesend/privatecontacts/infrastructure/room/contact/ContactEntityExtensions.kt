package ch.abwesend.privatecontacts.infrastructure.room.contact

import ch.abwesend.privatecontacts.domain.model.contact.Contact
import ch.abwesend.privatecontacts.domain.model.contact.ContactBase

private const val FULL_TEXT_SEARCH_ELEMENT_SEPARATOR = "|"

fun Contact.toEntity(): ContactEntity {
    val base = (this as ContactBase).toEntity()

    val contactData = contactDataSet
        .mapNotNull { it.formatValueForSearch() }
        .filter { it.isNotEmpty() }
        .joinToString(FULL_TEXT_SEARCH_ELEMENT_SEPARATOR)

    base.fullTextSearch += (FULL_TEXT_SEARCH_ELEMENT_SEPARATOR + contactData)
    return base
}

fun ContactBase.toEntity() = ContactEntity(
    id = id,
    firstName = firstName,
    lastName = lastName,
    nickname = nickname,
    type = type,
    notes = notes,
    fullTextSearch = listOf(
        firstName,
        lastName,
        nickname,
        notes
    ).joinToString(FULL_TEXT_SEARCH_ELEMENT_SEPARATOR)
)
