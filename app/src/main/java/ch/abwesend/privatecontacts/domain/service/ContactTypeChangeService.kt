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
import ch.abwesend.privatecontacts.domain.model.contact.withAccountInformation
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
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import kotlinx.coroutines.withContext

class ContactTypeChangeService {
    private val loadService: ContactLoadService by injectAnywhere()
    private val saveService: ContactSaveService by injectAnywhere()
    private val dispatchers: IDispatchers by injectAnywhere()

    // TODO add proper batch-processing (e.g. for the deletion of the old contacts)
    suspend fun changeContactType(
        contacts: Collection<IContactBase>,
        newType: ContactType
    ): ContactBatchChangeResult = withContext(dispatchers.default) {
        val strategy = ContactTypeChangeStrategy.fromContactType(newType)

        val baseContacts = contacts
            .filter { it.type != newType }
            .map { it.withAccountInformation() }
        val numberOfContacts = baseContacts.size

        val fullContactsByContactId = loadService.resolveContactsWithAccountInformation(baseContacts)
        val fullContacts = fullContactsByContactId.values.filterNotNull()
        logger.debug("Resolved ${fullContacts.size} of $numberOfContacts full contacts to change their type.")

        val numberOfUnresolvedContacts = fullContactsByContactId
            .filter { (_, contact) -> contact == null }
            .size
        if (numberOfUnresolvedContacts > 0) {
            // not shown to the user because those contacts, apparently no longer exist so the info is useless to them
            logger.warning("Failed to resolve $numberOfUnresolvedContacts of $numberOfContacts contacts.")
        }

        strategy.createContactGroups(fullContacts)

        val partialResults = fullContacts
            .mapAsyncChunked(chunkSize = 10) { contact -> contact.id to changeContactType(contact, strategy) }
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
        return if (newType == contact.type) {
            logger.debug("No contact-type change necessary: just save it normally.")
            saveService.saveContact(contact.asEditable())
        } else {
            val strategy = ContactTypeChangeStrategy.fromContactType(newType)
            changeContactType(contact, strategy)
        }
    }

    private suspend fun changeContactType(contact: IContact, strategy: ContactTypeChangeStrategy): ContactSaveResult {
        val newType = strategy.correspondingContactType
        logger.debug("Trying to change contact-type from ${contact.type} to $newType")
        return try {
            val contactEditable = contact.asEditable()
            changeContactContent(contactEditable, strategy)
        } catch (e: Exception) {
            logger.error("Failed to change contact type for ${contact.id}", e)
            Failure(UNKNOWN_ERROR)
        }
    }

    private suspend fun changeContactContent(
        contact: IContactEditable,
        strategy: ContactTypeChangeStrategy
    ): ContactSaveResult {
        val oldContact = contact.toContactBase()
        // the ID of the correct type will be set automatically, while saving
        val newContact = contact.deepCopy(isContactNew = true)

        newContact.type = strategy.correspondingContactType
        newContact.updateModelStatus()
        strategy.changeContactDataIds(newContact)

        return saveChangedContactAndDeleteOld(newContact, oldContact, strategy)
    }

    private fun IContactEditable.updateModelStatus() {
        val computeNewStatus: (oldStatus: ModelStatus) -> ModelStatus = { oldStatus ->
            when (oldStatus) {
                DELETED -> UNCHANGED // if it was to be deleted, just don't add it
                CHANGED, NEW, UNCHANGED -> NEW
            }
        }

        val newImageStatus = computeNewStatus(image.modelStatus)
        image = image.copy(modelStatus = newImageStatus)

        contactGroups.replaceAll { contactGroup ->
            val newStatus = computeNewStatus(contactGroup.modelStatus)
            contactGroup.copy(modelStatus = newStatus)
        }

        contactDataSet.replaceAll { contactData ->
            val newStatus = computeNewStatus(contactData.modelStatus)
            contactData.overrideStatus(newStatus)
        }

        contactGroups.replaceAll { contactGroup ->
            val newStatus = computeNewStatus(contactGroup.modelStatus)
            contactGroup.copy(modelStatus = newStatus)
        }
    }

    private suspend fun saveChangedContactAndDeleteOld(
        newContact: IContactEditable,
        oldContact: IContactBase,
        strategy: ContactTypeChangeStrategy,
    ): ContactSaveResult {
        val newType = strategy.correspondingContactType
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
                if (strategy.deleteOldContactAfterCreatingNew) {
                    deleteContactWithOldType(oldContact)
                } else saveResult
            }
        }
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
