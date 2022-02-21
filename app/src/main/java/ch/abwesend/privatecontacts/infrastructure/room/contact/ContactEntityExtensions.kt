/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.room.contact

import ch.abwesend.privatecontacts.domain.model.contact.IContact
import ch.abwesend.privatecontacts.domain.model.contact.IContactBase
import ch.abwesend.privatecontacts.domain.service.FullTextSearchService
import ch.abwesend.privatecontacts.domain.util.getAnywhere

fun IContact.toEntity(): ContactEntity {
    val fullTextSearchColumn = computeFullTextSearchColumn()
    return baseToEntity(fullTextSearchColumn)
}

private fun IContactBase.baseToEntity(fullTextSearch: String) = ContactEntity(
    rawId = id.uuid,
    firstName = firstName,
    lastName = lastName,
    nickname = nickname,
    type = type,
    notes = notes,
    fullTextSearch = fullTextSearch,
)

private fun IContact.computeFullTextSearchColumn(): String {
    val searchService: FullTextSearchService = getAnywhere()
    return searchService.computeFullTextSearchColumn(this)
}
