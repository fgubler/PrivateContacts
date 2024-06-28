/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.room.contact

import ch.abwesend.privatecontacts.domain.model.contact.ContactBase
import ch.abwesend.privatecontacts.domain.model.contact.IContact
import ch.abwesend.privatecontacts.domain.model.contact.IContactIdCombined
import ch.abwesend.privatecontacts.domain.model.contact.IContactIdInternal
import ch.abwesend.privatecontacts.domain.model.contact.getFullName
import ch.abwesend.privatecontacts.domain.service.FullTextSearchService
import ch.abwesend.privatecontacts.domain.util.getAnywhere

fun IContact.toEntity(contactId: IContactIdInternal): ContactEntity {
    val fullTextSearchColumn = computeFullTextSearchColumn()
    val externalContactNo = (contactId as? IContactIdCombined)?.contactNo

    return ContactEntity(
        rawId = contactId.uuid,
        externalContactNo = externalContactNo,
        importId = importId?.uuid,
        type = type,
        firstName = firstName,
        lastName = lastName,
        nickname = nickname,
        middleName = middleName,
        namePrefix = namePrefix,
        nameSuffix = nameSuffix,
        notes = notes,
        fullTextSearch = fullTextSearchColumn,
    )
}

private fun IContact.computeFullTextSearchColumn(): String {
    val searchService: FullTextSearchService = getAnywhere()
    return searchService.computeFullTextSearchColumn(this)
}

fun ContactEntity.toContactBase(): ContactBase =
    ContactBase(
        id = id,
        type = type,
        displayName = getFullName(firstName, lastName, nickname, middleName, namePrefix, nameSuffix),
    )
