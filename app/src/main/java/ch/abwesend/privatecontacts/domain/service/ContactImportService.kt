/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.service

import android.net.Uri
import ch.abwesend.privatecontacts.domain.lib.coroutine.IDispatchers
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.contact.ContactAccount
import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.model.contact.IContact
import ch.abwesend.privatecontacts.domain.model.contact.IContactEditable
import ch.abwesend.privatecontacts.domain.model.contact.IContactIdInternal
import ch.abwesend.privatecontacts.domain.model.contact.getFullName
import ch.abwesend.privatecontacts.domain.model.importexport.ContactImportData
import ch.abwesend.privatecontacts.domain.model.importexport.ContactImportPartialData.ParsedData
import ch.abwesend.privatecontacts.domain.model.importexport.ContactImportPartialData.SavedData
import ch.abwesend.privatecontacts.domain.model.importexport.VCardParseError
import ch.abwesend.privatecontacts.domain.model.importexport.VCardParseError.FILE_READING_FAILED
import ch.abwesend.privatecontacts.domain.model.result.ContactSaveResult
import ch.abwesend.privatecontacts.domain.model.result.ContactSaveResult.Success
import ch.abwesend.privatecontacts.domain.model.result.generic.BinaryResult
import ch.abwesend.privatecontacts.domain.repository.IContactRepository
import ch.abwesend.privatecontacts.domain.service.interfaces.IVCardImportExportRepository
import ch.abwesend.privatecontacts.domain.util.filterKeysNotNull
import ch.abwesend.privatecontacts.domain.util.filterValuesNotNull
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import kotlinx.coroutines.withContext
import java.util.UUID

class ContactImportService {
    private val dispatchers: IDispatchers by injectAnywhere()
    private val importExportRepository: IVCardImportExportRepository by injectAnywhere()
    private val fileReadService: FileReadWriteService by injectAnywhere()
    private val contactSaveService: ContactSaveService by injectAnywhere()
    private val contactRepository: IContactRepository by injectAnywhere()

    suspend fun importContacts(
        sourceFile: Uri,
        targetType: ContactType,
        targetAccount: ContactAccount,
        replaceExistingContacts: Boolean,
    ): BinaryResult<ContactImportData, VCardParseError> {
        val contactsToImport = loadContacts(sourceFile, targetType)
        return contactsToImport.mapValue { parsedContacts ->
            storeContacts(parsedContacts, targetType, targetAccount, replaceExistingContacts)
        }
    }

    suspend fun loadContacts(sourceFile: Uri, targetType: ContactType): BinaryResult<ParsedData, VCardParseError> =
        withContext(dispatchers.default) {
            val fileContentResult = fileReadService.readFileContent(sourceFile)
            fileContentResult
                .mapError { FILE_READING_FAILED }
                .mapValueToBinaryResult { fileContent ->
                    importExportRepository.parseContacts(fileContent, targetType)
                }
        }

    suspend fun storeContacts(
        parsedContacts: ParsedData,
        targetType: ContactType,
        targetAccount: ContactAccount,
        replaceExistingContacts: Boolean,
    ): ContactImportData = withContext(dispatchers.default) {
        val contactsToSave = parsedContacts.successfulContacts
        postProcessContacts(contactsToSave, targetType, targetAccount)

        val savedData = saveImportedContacts(contactsToSave, replaceExistingContacts)

        ContactImportData(
            newImportedContacts = savedData.newImportedContacts,
            replacedExistingContacts = savedData.replacedExistingContacts,
            importFailures = savedData.importFailures,
            importValidationFailures = savedData.importValidationFailures,
            numberOfParsingFailures = parsedContacts.numberOfFailedContacts,
        )
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

    private suspend fun saveImportedContacts(
        contactsToSave: List<IContactEditable>,
        replaceExistingContacts: Boolean,
    ): SavedData {
        val saveResults = contactSaveService.saveContacts(contactsToSave)
        val successfullySavedContacts = saveResults.filterValues { it is Success }.keys.toList()

        val replacedContacts: List<IContact> = if (replaceExistingContacts) {
            determineExistingContactsForDeletion(successfullySavedContacts).also { replacedContacts ->
                val contactIdsToDelete = replacedContacts.map { it.id }.toSet()
                contactSaveService.deleteContacts(contactIdsToDelete)
            }
        } else emptyList()

        return SavedData(
            newImportedContacts = successfullySavedContacts,
            importValidationFailures = saveResults
                .mapValues { it.value as? ContactSaveResult.ValidationFailure }
                .filterValuesNotNull()
                .mapValues { it.value.validationErrors },
            importFailures = saveResults
                .mapValues { it.value as? ContactSaveResult.Failure }
                .filterValuesNotNull()
                .mapValues { it.value.errors },
            replacedExistingContacts = replacedContacts,
        )
    }

    /**
     * When importing a contact which already exists, a new contact is created.
     * Probably, the user wants to delete the old ones.
     * Can only replace secret contacts because the public ones don't have UUIDs.
     */
    private suspend fun determineExistingContactsForDeletion(newContacts: List<IContactEditable>): List<IContact> {
        logger.debug("Determining existing, replaced contacts for deletion")

        val importIds = newContacts.mapNotNull { it.importId }
        val newContactIds = newContacts.map { it.id }.toSet()
        val existingContacts: List<IContact> = contactRepository.resolveMatchingContacts(importIds)
        val replacingContactsByImportId = newContacts.associateBy { it.importId?.uuid }.filterKeysNotNull()

        return if (existingContacts.any()) {
            val contactsToDelete = existingContacts
                .filterNot { contact -> newContactIds.contains(contact.id) } // do not delete what we just imported
                .filter { existingContact -> shouldBeReplaced(existingContact, replacingContactsByImportId) }

            logger.debug("Marked ${contactsToDelete.size} contacts for deletion")
            contactsToDelete
        } else emptyList()
    }

    private fun shouldBeReplaced(
        existingContact: IContact,
        replacingContactsByImportUuid: Map<UUID, IContact>,
    ): Boolean {
        val contactUuid = (existingContact.id as? IContactIdInternal)?.uuid
        val contactImportUuid = existingContact.importId?.uuid

        val replacingContact = replacingContactsByImportUuid[contactUuid]
            ?: replacingContactsByImportUuid[contactImportUuid]

        return replacingContact?.considerAsSamePerson(existingContact) == true
    }

    private fun IContact.considerAsSamePerson(other: IContact): Boolean = (
        (firstName == other.firstName && lastName == other.lastName) ||
            (firstName == other.lastName && lastName == other.firstName)
        ) && (
        getFullName(firstNameFirst = true) == other.getFullName(firstNameFirst = true) ||
            getFullName(firstNameFirst = false) == other.getFullName(firstNameFirst = false) ||
            getFullName(firstNameFirst = true) == other.getFullName(firstNameFirst = false) ||
            getFullName(firstNameFirst = false) == other.getFullName(firstNameFirst = true)
        )
}
