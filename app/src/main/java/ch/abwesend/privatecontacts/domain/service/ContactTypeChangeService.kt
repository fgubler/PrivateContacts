/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.service

import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.ModelStatus
import ch.abwesend.privatecontacts.domain.model.ModelStatus.DELETED
import ch.abwesend.privatecontacts.domain.model.ModelStatus.NEW
import ch.abwesend.privatecontacts.domain.model.ModelStatus.UNCHANGED
import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.model.contact.IContactBase
import ch.abwesend.privatecontacts.domain.model.contact.IContactEditable
import ch.abwesend.privatecontacts.domain.model.contact.toContactBase
import ch.abwesend.privatecontacts.domain.model.result.ContactChangeError.NOT_YET_IMPLEMENTED_FOR_INTERNAL_CONTACTS
import ch.abwesend.privatecontacts.domain.model.result.ContactChangeError.UNABLE_TO_CREATE_CONTACT_WITH_NEW_TYPE
import ch.abwesend.privatecontacts.domain.model.result.ContactChangeError.UNABLE_TO_DELETE_CONTACT_WITH_OLD_TYPE
import ch.abwesend.privatecontacts.domain.model.result.ContactDeleteResult
import ch.abwesend.privatecontacts.domain.model.result.ContactSaveResult
import ch.abwesend.privatecontacts.domain.model.result.ContactSaveResult.ValidationFailure
import ch.abwesend.privatecontacts.domain.util.injectAnywhere

class ContactTypeChangeService {
    private val saveService: ContactSaveService by injectAnywhere()

    suspend fun changeContactType(contact: IContactEditable, newType: ContactType): ContactSaveResult {
        logger.debug("Trying to change contact-type from ${contact.type} to $newType")

        return when (newType) {
            contact.type -> {
                logger.debug("No contact-type change necessary: just save it normally.")
                saveService.saveContact(contact)
            }
            ContactType.PUBLIC -> ContactSaveResult.Failure(NOT_YET_IMPLEMENTED_FOR_INTERNAL_CONTACTS)
            ContactType.SECRET -> changeContactTypeToSecret(contact, newType)
        }
    }

    private suspend fun changeContactTypeToSecret(contact: IContactEditable, newType: ContactType): ContactSaveResult {
        val oldContact = contact.toContactBase()
        val newContact = contact.changeToInternalId()
        newContact.type = newType
        newContact.setModelStatusNew()

        logger.debug("Saving contact with new type $newType")
        val saveResult = saveService.saveContact(newContact)

        return when(saveResult) {

            is ValidationFailure -> {
                logger.warning("Failed to save contact due to validation")
                saveResult
            }
            is ContactSaveResult.Failure -> {
                logger.warning("Failed to save contact ${oldContact.id} with new type $newType")
                val errors = listOf(UNABLE_TO_CREATE_CONTACT_WITH_NEW_TYPE)
                ContactSaveResult.Failure(errors + saveResult.errors)
            }
            is ContactSaveResult.Success -> {
                logger.debug("Successfully saved contact with new type $newType")
                deleteContactWithOldType(oldContact)
            }
        }
    }

    private fun IContactEditable.setModelStatusNew() {
        val computeNewStatus: (oldStatus: ModelStatus) -> ModelStatus = { oldStatus ->
            // if it was to be deleted, just don't add it
            if (oldStatus == DELETED) UNCHANGED else NEW
        }

        val newImageStatus = computeNewStatus(image.modelStatus)
        image = image.copy(modelStatus = newImageStatus)

        contactDataSet.replaceAll { contactData ->
            val newStatus = computeNewStatus(contactData.modelStatus)
            contactData.overrideStatus(newStatus)
        }
    }

    private suspend fun deleteContactWithOldType(contact: IContactBase): ContactSaveResult {
        logger.debug("Deleting old contact with id ${contact.id}")
        val deleteResult = saveService.deleteContact(contact)
        return when(deleteResult) {
            is ContactDeleteResult.Failure -> {
                logger.warning("Failed to delete old contact with id ${contact.id}")
                val errors = listOf(UNABLE_TO_DELETE_CONTACT_WITH_OLD_TYPE)
                ContactSaveResult.Failure(errors + deleteResult.errors)
            }
            ContactDeleteResult.Success -> {
                logger.debug("Deleted old contact with id ${contact.id}")
                ContactSaveResult.Success
            }
        }
    }
}
