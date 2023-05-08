/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.service

import android.net.Uri
import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.model.result.ContactImportResult
import ch.abwesend.privatecontacts.domain.service.interfaces.IContactImportExportRepository
import ch.abwesend.privatecontacts.domain.util.injectAnywhere

// TODO add unit tests
class ContactImportExportService {
    private val importExportService: IContactImportExportRepository by injectAnywhere()
    private val fileReadService: FileReadService by injectAnywhere()
    private val contactSaveService: ContactSaveService by injectAnywhere()

    suspend fun importContacts(sourceFile: Uri, targetType: ContactType): ContactImportResult {
        val fileContentResult = fileReadService.readFileContent(sourceFile)
        val contactsToImport = fileContentResult.mapValueSuspending { fileContent ->
            importExportService.loadContacts(fileContent, targetType)
        }
        val result = contactsToImport.mapValueSuspending { loadResult ->
            // TODO refactor to use the Result class as well
            when (loadResult) {
                is ContactImportResult.FileReadingFailed, is ContactImportResult.VcfParsingFailed -> loadResult
                is ContactImportResult.Success -> {
                    // TODO create contacts
                }
            }
        }

        return ContactImportResult.Success(emptyList(), 0) // TODO replace with real result.
    }
}
