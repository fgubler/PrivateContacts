/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.service

import android.net.Uri
import ch.abwesend.privatecontacts.domain.lib.coroutine.IDispatchers
import ch.abwesend.privatecontacts.domain.model.contact.ContactAccount
import ch.abwesend.privatecontacts.domain.model.contact.ContactId
import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.model.contact.IContactEditable
import ch.abwesend.privatecontacts.domain.model.importexport.ContactImportData
import ch.abwesend.privatecontacts.domain.model.importexport.ContactImportPartialData.SavedData
import ch.abwesend.privatecontacts.domain.model.importexport.VCardParseError
import ch.abwesend.privatecontacts.domain.model.importexport.VCardParseError.FILE_READING_FAILED
import ch.abwesend.privatecontacts.domain.model.result.ContactSaveResult
import ch.abwesend.privatecontacts.domain.model.result.ContactSaveResult.Success
import ch.abwesend.privatecontacts.domain.model.result.generic.BinaryResult
import ch.abwesend.privatecontacts.domain.service.interfaces.IVCardImportExportRepository
import ch.abwesend.privatecontacts.domain.util.filterValuesNotNull
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import kotlinx.coroutines.withContext

class ContactImportService {
    private val dispatchers: IDispatchers by injectAnywhere()
    private val importExportRepository: IVCardImportExportRepository by injectAnywhere()
    private val fileReadService: FileReadWriteService by injectAnywhere()
    private val contactSaveService: ContactSaveService by injectAnywhere()

    suspend fun importContacts(
        sourceFile: Uri,
        targetType: ContactType,
        targetAccount: ContactAccount
    ): BinaryResult<ContactImportData, VCardParseError> = withContext(dispatchers.default) {
        val fileContentResult = fileReadService.readFileContent(sourceFile)
        val contactsToImport = fileContentResult
            .mapError { FILE_READING_FAILED }
            .mapValueToBinaryResult { fileContent ->
                importExportRepository.parseContacts(fileContent, targetType)
            }

        contactsToImport.mapValue { parsedContacts ->
            val contactsToSave = parsedContacts.successfulContacts
            postProcessContacts(contactsToSave, targetType, targetAccount)

            val savedData = saveImportedContacts(contactsToSave)

            ContactImportData(
                newImportedContacts = savedData.newImportedContacts,
                existingIgnoredContacts = savedData.existingIgnoredContacts,
                importFailures = savedData.importFailures,
                importValidationFailures = savedData.importValidationFailures,
                numberOfParsingFailures = parsedContacts.numberOfFailedContacts,
            )
        }
    }

    private fun postProcessContacts(
        contacts: List<IContactEditable>,
        targetType: ContactType,
        targetAccount: ContactAccount
    ) {
        contacts.forEach { contact ->
            contact.type = targetType
            contact.saveInAccount = targetAccount
        }
    }

    private suspend fun saveImportedContacts(contacts: List<IContactEditable>): SavedData {
        val existingContactIds: List<ContactId> = emptyList() // TODO implement a merging strategy (and add test)
        val ignoredExistingContacts = contacts.filter { existingContactIds.contains(it.id) }
        val newContacts = contacts.filterNot { existingContactIds.contains(it.id) }
        val saveResults = contactSaveService.saveContacts(newContacts)

        return SavedData(
            newImportedContacts = saveResults.filterValues { it is Success }.keys.toList(),
            importValidationFailures = saveResults
                .mapValues { it.value as? ContactSaveResult.ValidationFailure }
                .filterValuesNotNull()
                .mapValues { it.value.validationErrors },
            importFailures = saveResults
                .mapValues { it.value as? ContactSaveResult.Failure }
                .filterValuesNotNull()
                .mapValues { it.value.errors },
            existingIgnoredContacts = ignoredExistingContacts,
        )
    }
}
