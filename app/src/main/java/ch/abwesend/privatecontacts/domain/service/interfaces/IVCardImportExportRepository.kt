/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.service.interfaces

import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.model.contact.IContact
import ch.abwesend.privatecontacts.domain.model.importexport.ContactExportPartialData
import ch.abwesend.privatecontacts.domain.model.importexport.ContactImportPartialData
import ch.abwesend.privatecontacts.domain.model.importexport.TextFileContent
import ch.abwesend.privatecontacts.domain.model.importexport.VCardCreateError
import ch.abwesend.privatecontacts.domain.model.importexport.VCardParseError
import ch.abwesend.privatecontacts.domain.model.importexport.VCardVersion
import ch.abwesend.privatecontacts.domain.model.result.generic.BinaryResult

interface IVCardImportExportRepository {
    suspend fun exportContacts(contacts: List<IContact>, vCardVersion: VCardVersion): VCardCreationResult

    /**
     * the contacts are created as internal contacts
     * i.e. with internal IDs for the contacts and their data
     */
    suspend fun parseContacts(fileContent: TextFileContent, targetType: ContactType): ContactParseResult
}

typealias ContactParseResult = BinaryResult<ContactImportPartialData.ParsedData, VCardParseError>
typealias VCardCreationResult = BinaryResult<ContactExportPartialData.CreatedVCards, VCardCreateError>
