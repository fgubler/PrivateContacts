/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.service

import ch.abwesend.privatecontacts.domain.model.contact.IContact
import ch.abwesend.privatecontacts.domain.model.contactdata.PhoneNumber
import ch.abwesend.privatecontacts.domain.model.contactdata.StringBasedContactData
import ch.abwesend.privatecontacts.domain.util.getAnywhere

class FullTextSearchService {
    fun isLongEnough(query: String): Boolean = query.length >= SEARCH_MIN_LENGTH

    fun prepareQuery(query: String): String =
        StringBasedContactData.formatValueForSearch(query)

    fun prepareQueryForPhoneNumberSearch(query: String): String =
        PhoneNumber.formatValueForSearch(query)

    fun computeFullTextSearchColumn(contact: IContact): String = with(contact) {
        val baseData = listOf(
            "$firstName $lastName",
            "$lastName $firstName",
            firstName,
            lastName,
            nickname,
            middleName,
            namePrefix,
            nameSuffix,
            notes
        )

        val additionalData = contactDataSet
            .mapNotNull { it.formatValueForSearch() }
            .filter { it.isNotEmpty() }

        val groupData = contactGroups.map { it.id.name }

        val service: FullTextSearchService = getAnywhere()
        val allData = baseData + additionalData + groupData

        return allData
            .map { service.prepareQuery(it) }
            .joinToString(FULL_TEXT_SEARCH_ELEMENT_SEPARATOR)
    }

    companion object {
        private const val SEARCH_MIN_LENGTH = 3
        private const val FULL_TEXT_SEARCH_ELEMENT_SEPARATOR = "|"
    }
}
