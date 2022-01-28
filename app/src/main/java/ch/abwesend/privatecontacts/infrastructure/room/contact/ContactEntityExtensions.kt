package ch.abwesend.privatecontacts.infrastructure.room.contact

import ch.abwesend.privatecontacts.domain.model.contact.IContact
import ch.abwesend.privatecontacts.domain.model.contact.IContactBase

private const val FULL_TEXT_SEARCH_ELEMENT_SEPARATOR = "|"

fun IContact.toEntity(): ContactEntity {
    val base = (this as IContactBase).toEntity()

    val contactData = contactDataSet
        .mapNotNull { it.formatValueForSearch() }
        .filter { it.isNotEmpty() }
        .joinToString(FULL_TEXT_SEARCH_ELEMENT_SEPARATOR)

    base.fullTextSearch += (FULL_TEXT_SEARCH_ELEMENT_SEPARATOR + contactData)
    return base
}

fun IContactBase.toEntity() = ContactEntity(
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
