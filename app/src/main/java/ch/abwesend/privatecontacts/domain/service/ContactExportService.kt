/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.service

import android.net.Uri
import ch.abwesend.privatecontacts.domain.lib.coroutine.IDispatchers
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.model.contact.IContact
import ch.abwesend.privatecontacts.domain.model.importexport.ContactExportData
import ch.abwesend.privatecontacts.domain.model.importexport.TextFileContent
import ch.abwesend.privatecontacts.domain.model.importexport.VCardExportError
import ch.abwesend.privatecontacts.domain.model.importexport.VCardExportError.FILE_WRITING_FAILED
import ch.abwesend.privatecontacts.domain.model.importexport.VCardVersion
import ch.abwesend.privatecontacts.domain.model.result.generic.BinaryResult
import ch.abwesend.privatecontacts.domain.model.result.generic.ifError
import ch.abwesend.privatecontacts.domain.model.result.generic.mapError
import ch.abwesend.privatecontacts.domain.model.result.generic.mapValue
import ch.abwesend.privatecontacts.domain.model.result.generic.mapValueToResult
import ch.abwesend.privatecontacts.domain.repository.IEncryptionRepository
import ch.abwesend.privatecontacts.domain.repository.IFileAccessRepository
import ch.abwesend.privatecontacts.domain.service.interfaces.IVCardImportExportRepository
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import kotlinx.coroutines.withContext

class ContactExportService {
    private val dispatchers: IDispatchers by injectAnywhere()
    private val loadService: ContactLoadService by injectAnywhere()
    private val fileWriteService: FileReadWriteService by injectAnywhere()
    private val importExportRepository: IVCardImportExportRepository by injectAnywhere()
    private val fileAccessRepository: IFileAccessRepository by injectAnywhere()
    private val encryptionRepository: IEncryptionRepository by injectAnywhere()

    suspend fun exportContacts(
        targetFile: Uri,
        sourceType: ContactType,
        vCardVersion: VCardVersion,
        requestPermission: Boolean = true,
        encryptionPassword: String? = null,
    ): BinaryResult<ContactExportData, VCardExportError> = withContext(dispatchers.default) {
        val contacts = loadService.loadFullContactsByType(sourceType)
        exportContacts(targetFile, vCardVersion, contacts, requestPermission, encryptionPassword)
    }

    suspend fun exportContacts(
        targetFile: Uri,
        vCardVersion: VCardVersion,
        contacts: List<IContact>,
        requestPermission: Boolean = true,
        encryptionPassword: String? = null,
    ): BinaryResult<ContactExportData, VCardExportError> = withContext(dispatchers.default) {
        val vCardResult = importExportRepository.exportContacts(contacts, vCardVersion)
            .ifError { logger.warning("Failed to create vCards for contacts: $it") }

        val fileWriteResult = vCardResult.mapValueToResult { createdVCards ->
            val writeResult = if (encryptionPassword == null) {
                fileWriteService.writeContentToFile(
                    content = createdVCards.fileContent,
                    fileUri = targetFile,
                    requestPermission = requestPermission
                )
            } else {
                val encryptionResult = encryptionRepository.encrypt(
                    plaintext = createdVCards.fileContent.content,
                    password = encryptionPassword
                )
                encryptionResult.mapValueToResult { ciphertext ->
                    fileAccessRepository.writeFile(
                        fileContent = TextFileContent(ciphertext),
                        file = targetFile,
                        requestPermission = requestPermission
                    )
                }
            }

            writeResult
                .mapValue {
                    val failedContacts = createdVCards.failedContacts
                    val successfulContacts = contacts.minus(failedContacts.toSet())
                    ContactExportData(successfulContacts = successfulContacts, failedContacts = failedContacts)
                }
                .ifError { logger.warning("Failed to export vCards to file: $it") }
                .mapError { FILE_WRITING_FAILED }
        }
        fileWriteResult
    }
}
