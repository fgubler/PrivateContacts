/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.service

import ch.abwesend.privatecontacts.domain.lib.coroutine.IDispatchers
import ch.abwesend.privatecontacts.domain.lib.coroutine.mapAsyncChunked
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.ModelStatus
import ch.abwesend.privatecontacts.domain.model.ModelStatus.CHANGED
import ch.abwesend.privatecontacts.domain.model.ModelStatus.DELETED
import ch.abwesend.privatecontacts.domain.model.ModelStatus.NEW
import ch.abwesend.privatecontacts.domain.model.ModelStatus.UNCHANGED
import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.model.contact.IContact
import ch.abwesend.privatecontacts.domain.model.contact.IContactBase
import ch.abwesend.privatecontacts.domain.model.contact.IContactEditable
import ch.abwesend.privatecontacts.domain.model.contact.asEditable
import ch.abwesend.privatecontacts.domain.model.contact.toContactBase
import ch.abwesend.privatecontacts.domain.model.result.ContactChangeError.NOT_YET_IMPLEMENTED_FOR_INTERNAL_CONTACTS
import ch.abwesend.privatecontacts.domain.model.result.ContactChangeError.UNABLE_TO_CREATE_CONTACT_WITH_NEW_TYPE
import ch.abwesend.privatecontacts.domain.model.result.ContactChangeError.UNABLE_TO_DELETE_CONTACT_WITH_OLD_TYPE
import ch.abwesend.privatecontacts.domain.model.result.ContactChangeError.UNKNOWN_ERROR
import ch.abwesend.privatecontacts.domain.model.result.ContactDeleteResult
import ch.abwesend.privatecontacts.domain.model.result.ContactSaveResult
import ch.abwesend.privatecontacts.domain.model.result.ContactSaveResult.Failure
import ch.abwesend.privatecontacts.domain.model.result.ContactSaveResult.Success
import ch.abwesend.privatecontacts.domain.model.result.ContactSaveResult.ValidationFailure
import ch.abwesend.privatecontacts.domain.model.result.batch.ContactBatchChangeErrors
import ch.abwesend.privatecontacts.domain.model.result.batch.ContactBatchChangeResult
import ch.abwesend.privatecontacts.domain.model.result.errorsOrEmpty
import ch.abwesend.privatecontacts.domain.model.result.validationErrorsOrEmpty
import ch.abwesend.privatecontacts.domain.repository.IContactGroupRepository
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import kotlinx.coroutines.withContext

class ContactTypeChangeService {
    private val loadService: ContactLoadService by injectAnywhere()
    private val saveService: ContactSaveService by injectAnywhere()
    private val contactGroupRepository: IContactGroupRepository by injectAnywhere()
    private val dispatchers: IDispatchers by injectAnywhere()

    // TODO add proper batch-processing (e.g. for the deletion of the old contacts)
    suspend fun changeContactType(
        contacts: Collection<IContactBase>,
        newType: ContactType
    ): ContactBatchChangeResult = withContext(dispatchers.default) {
        val contactIds = contacts
            .filter { it.type != newType }
            .map { it.id }
        val numberOfContacts = contactIds.size

        val fullContactsByContactId = loadService.resolveContacts(contactIds)
        val fullContacts = fullContactsByContactId.values.filterNotNull()
        logger.debug("Resolved ${fullContacts.size} of $numberOfContacts full contacts to change their type.")

        val numberOfUnresolvedContacts = fullContactsByContactId
            .filter { (_, contact) -> contact == null }
            .size
        if (numberOfUnresolvedContacts > 0) {
            // not shown to the user because those contacts, apparently no longer exist so the info is useless to them
            logger.warning("Failed to resolve $numberOfUnresolvedContacts of $numberOfContacts contacts.")
        }

        val contactGroups = fullContacts.flatMap { it.contactGroups }.distinctBy { it.id }
        contactGroupRepository.createMissingContactGroups(contactGroups) // avoid race-conditions later on

        val partialResults = fullContacts
            .mapAsyncChunked { contact -> contact.id to changeContactType(contact, newType) }
            .toMap()

        val successfulContacts = partialResults.filterValues { it is Success }.map { it.key }.toSet()
        val failedContacts = partialResults
            .filter { (contactId, _) -> !successfulContacts.contains(contactId) }
            .mapValues { (_, saveResult) ->
                ContactBatchChangeErrors(
                    errors = saveResult.errorsOrEmpty,
                    validationErrors = saveResult.validationErrorsOrEmpty
                )
            }

        logger.debug("Changed contact-type: ${successfulContacts.size} successful, ${failedContacts.size} failed.")

        ContactBatchChangeResult(
            successfulChanges = successfulContacts.toList(),
            failedChanges = failedContacts,
        )
    }

    suspend fun changeContactType(contact: IContact, newType: ContactType): ContactSaveResult {
        logger.debug("Trying to change contact-type from ${contact.type} to $newType")
        return try {
            val contactEditable = contact.asEditable()
            when (newType) {
                contact.type -> {
                    logger.debug("No contact-type change necessary: just save it normally.")
                    saveService.saveContact(contactEditable)
                }
                ContactType.PUBLIC -> Failure(NOT_YET_IMPLEMENTED_FOR_INTERNAL_CONTACTS)
                ContactType.SECRET -> changeContactTypeToSecret(contactEditable, newType)
            }
        } catch (e: Exception) {
            logger.error("Failed to change contact type for ${contact.id}", e)
            Failure(UNKNOWN_ERROR)
        }
    }

    private suspend fun changeContactTypeToSecret(contact: IContactEditable, newType: ContactType): ContactSaveResult {
        val oldContact = contact.toContactBase()
        // the internal ID will be set automatically, while saving
        val newContact = contact.deepCopy(isContactNew = true)
        newContact.type = newType
        newContact.setModelStatusNew()
        newContact.changeContactDataToInternalIds()

        logger.debug("Saving contact with new type $newType")
        val saveResult = saveService.saveContact(newContact)

        return when (saveResult) {
            is ValidationFailure -> {
                logger.warning("Failed to save contact due to validation")
                saveResult
            }
            is Failure -> {
                logger.warning("Failed to save contact ${oldContact.id} with new type $newType")
                val errors = listOf(UNABLE_TO_CREATE_CONTACT_WITH_NEW_TYPE)
                Failure(errors + saveResult.errors)
            }
            is Success -> {
                logger.debug("Successfully saved contact with new type $newType")
                deleteContactWithOldType(oldContact)
            }
        }
    }

    private fun IContactEditable.setModelStatusNew() {
        val computeNewStatus: (oldStatus: ModelStatus) -> ModelStatus = { oldStatus ->
            when (oldStatus) {
                DELETED -> UNCHANGED // if it was to be deleted, just don't add it
                CHANGED, NEW, UNCHANGED -> NEW
            }
        }

        val newImageStatus = computeNewStatus(image.modelStatus)
        image = image.copy(modelStatus = newImageStatus)

        contactDataSet.replaceAll { contactData ->
            val newStatus = computeNewStatus(contactData.modelStatus)
            contactData.overrideStatus(newStatus)
        }
    }

    private fun IContactEditable.changeContactDataToInternalIds() {
        contactDataSet.replaceAll { contactData -> contactData.changeToInternalId() }
    }

    private suspend fun deleteContactWithOldType(contact: IContactBase): ContactSaveResult {
        logger.debug("Deleting old contact with id ${contact.id}")
        val deleteResult = saveService.deleteContact(contact)
        return when (deleteResult) {
            is ContactDeleteResult.Failure -> {
                logger.warning("Failed to delete old contact with id ${contact.id}")
                val errors = listOf(UNABLE_TO_DELETE_CONTACT_WITH_OLD_TYPE)
                Failure(errors + deleteResult.errors)
            }
            ContactDeleteResult.Success -> {
                logger.debug("Deleted old contact with id ${contact.id}")
                Success
            }
        }
    }
}
