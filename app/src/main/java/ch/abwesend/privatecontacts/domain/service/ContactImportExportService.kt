/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.service

import android.net.Uri
import ch.abwesend.privatecontacts.domain.model.contact.ContactAccount
import ch.abwesend.privatecontacts.domain.model.contact.ContactId
import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.model.contact.IContact
import ch.abwesend.privatecontacts.domain.model.contact.IContactEditable
import ch.abwesend.privatecontacts.domain.model.contact.IContactIdInternal
import ch.abwesend.privatecontacts.domain.model.importexport.ContactImportData
import ch.abwesend.privatecontacts.domain.model.importexport.ContactImportPartialData.SavedData
import ch.abwesend.privatecontacts.domain.model.importexport.VCardParseError
import ch.abwesend.privatecontacts.domain.model.importexport.VCardParseError.FILE_READING_FAILED
import ch.abwesend.privatecontacts.domain.model.result.BinaryResult
import ch.abwesend.privatecontacts.domain.model.result.ContactSaveResult
import ch.abwesend.privatecontacts.domain.model.result.ContactSaveResult.Success
import ch.abwesend.privatecontacts.domain.service.interfaces.IVCardImportExportRepository
import ch.abwesend.privatecontacts.domain.util.filterValuesNotNull
import ch.abwesend.privatecontacts.domain.util.injectAnywhere

// TODO add unit tests
class ContactImportExportService {
    private val importExportService: IVCardImportExportRepository by injectAnywhere()
    private val fileReadService: FileReadService by injectAnywhere()
    private val contactSaveService: ContactSaveService by injectAnywhere()
    private val contactLoadService: ContactLoadService by injectAnywhere()

    suspend fun importContacts(
        sourceFile: Uri,
        targetType: ContactType,
        targetAccount: ContactAccount
    ): BinaryResult<ContactImportData, VCardParseError> {
        val fileContentResult = fileReadService.readFileContent(sourceFile)
        val contactsToImport = fileContentResult
            .mapError { FILE_READING_FAILED }
            .mapValueToBinaryResult { fileContent ->
                importExportService.parseContacts(fileContent, targetType)
            }

        return contactsToImport.mapValue { parsedContacts ->
            val contactsToSave = parsedContacts.successfulContacts
            postProcessContacts(contactsToSave, targetType, targetAccount)

            val savedData = saveImportedContacts(contactsToSave, targetType)

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

    private suspend fun saveImportedContacts(contacts: List<IContactEditable>, targetType: ContactType): SavedData {
        val existingContactIds = loadExistingContactIds(contacts, targetType)
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

    /**
     * The contact-IDs in VCF are based on UUIDs, so external contacts cannot be matched.
     */
    private suspend fun loadExistingContactIds(contacts: List<IContact>, targetType: ContactType): Set<ContactId> =
        when (targetType) {
            ContactType.PUBLIC -> emptySet()
            ContactType.SECRET -> {
                val contactIds = contacts.map { it.id }.filterIsInstance<IContactIdInternal>()
                contactLoadService.filterForExistingContacts(contactIds)
            }
        }
}
