/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.service

import android.net.Uri
import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.model.importexport.ContactImportResult
import ch.abwesend.privatecontacts.domain.model.importexport.ContactParseError.FILE_READING_FAILED
import ch.abwesend.privatecontacts.domain.service.interfaces.IContactImportExportRepository
import ch.abwesend.privatecontacts.domain.util.injectAnywhere

// TODO add unit tests
class ContactImportExportService {
    private val importExportService: IContactImportExportRepository by injectAnywhere()
    private val fileReadService: FileReadService by injectAnywhere()
    private val contactSaveService: ContactSaveService by injectAnywhere()

    suspend fun importContacts(sourceFile: Uri, targetType: ContactType): ContactImportResult {
        val fileContentResult = fileReadService.readFileContent(sourceFile)
        val contactsToImport = fileContentResult
            .mapError { FILE_READING_FAILED }
            .mapValueToBinaryResult { fileContent ->
                importExportService.parseContacts(fileContent, targetType)
            }

        val importedContacts = contactsToImport.mapValue { parsedContacts ->
            // TODO create contacts
        }

        return ContactImportResult(
            newImportedContacts = emptyList(),
            existingIgnoredContacts = emptyList(),
            existingReplacedContacts = emptyList(),
            numberOfParsingFailures = 0,
            importFailures = emptyMap(),
        ) // TODO replace with real result.
    }
}
