/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.service

import android.net.Uri
import ch.abwesend.privatecontacts.domain.lib.coroutine.IDispatchers
import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.model.contact.IContact
import ch.abwesend.privatecontacts.domain.model.importexport.ContactExportData
import ch.abwesend.privatecontacts.domain.model.importexport.VCardCreateError
import ch.abwesend.privatecontacts.domain.model.importexport.VCardCreateError.FILE_WRITING_FAILED
import ch.abwesend.privatecontacts.domain.model.importexport.VCardVersion
import ch.abwesend.privatecontacts.domain.model.result.generic.BinaryResult
import ch.abwesend.privatecontacts.domain.service.interfaces.IVCardImportExportRepository
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import kotlinx.coroutines.withContext

// TODO add unit tests
class ContactExportService {
    private val dispatchers: IDispatchers by injectAnywhere()
    private val loadService: ContactLoadService by injectAnywhere()
    private val fileWriteService: FileReadWriteService by injectAnywhere()
    private val importExportRepository: IVCardImportExportRepository by injectAnywhere()

    suspend fun exportContacts(
        targetFile: Uri,
        sourceType: ContactType,
        vCardVersion: VCardVersion,
    ): BinaryResult<ContactExportData, VCardCreateError> = withContext(dispatchers.default) {
        val contacts = loadService.loadFullContactsByType(sourceType)
        exportContacts(targetFile, vCardVersion, contacts)
    }

    suspend fun exportContacts(
        targetFile: Uri,
        vCardVersion: VCardVersion,
        contacts: List<IContact>,
    ): BinaryResult<ContactExportData, VCardCreateError> = withContext(dispatchers.default) {
        val vCardResult = importExportRepository.exportContacts(contacts, vCardVersion)

        val fileWriteResult = vCardResult.mapValueToBinaryResult { createdVCards ->
            fileWriteService.writeContentToFile(createdVCards.fileContent, targetFile)
                .mapValue {
                    val failedContacts = createdVCards.failedContacts
                    val successfulContacts = contacts.minus(failedContacts.toSet())
                    ContactExportData(successfulContacts = successfulContacts, failedContacts = failedContacts)
                }
                .mapError { FILE_WRITING_FAILED }
        }
        fileWriteResult
    }
}
