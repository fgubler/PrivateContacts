/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.service.interfaces

import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.model.contact.IContact
import ch.abwesend.privatecontacts.domain.model.importexport.ContactParseError
import ch.abwesend.privatecontacts.domain.model.importexport.ContactParsedData
import ch.abwesend.privatecontacts.domain.model.importexport.FileContent
import ch.abwesend.privatecontacts.domain.model.result.BinaryResult
import ch.abwesend.privatecontacts.domain.model.result.ContactExportResult

interface IContactImportExportRepository {
    suspend fun exportContacts(contacts: List<IContact>): ContactExportResult

    /**
     * the contacts are created as internal contacts
     * i.e. with internal IDs for the contacts and their data
     */
    suspend fun parseContacts(fileContent: FileContent, targetType: ContactType): ContactParseResult
}

typealias ContactParseResult = BinaryResult<ContactParsedData, ContactParseError>
