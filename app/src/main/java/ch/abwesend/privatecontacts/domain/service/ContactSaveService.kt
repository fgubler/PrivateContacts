/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.service

import ch.abwesend.privatecontacts.domain.model.contact.ContactId
import ch.abwesend.privatecontacts.domain.model.contact.ContactIdInternal
import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.model.contact.IContactBase
import ch.abwesend.privatecontacts.domain.model.contact.IContactEditable
import ch.abwesend.privatecontacts.domain.model.contact.IContactIdExternal
import ch.abwesend.privatecontacts.domain.model.contact.IContactIdInternal
import ch.abwesend.privatecontacts.domain.model.contactdata.IContactDataIdExternal
import ch.abwesend.privatecontacts.domain.model.contactdata.IContactDataIdInternal
import ch.abwesend.privatecontacts.domain.model.result.ContactChangeError.UNABLE_TO_DELETE_CONTACT
import ch.abwesend.privatecontacts.domain.model.result.ContactDeleteResult
import ch.abwesend.privatecontacts.domain.model.result.ContactSaveResult
import ch.abwesend.privatecontacts.domain.model.result.ContactSaveResult.ValidationFailure
import ch.abwesend.privatecontacts.domain.model.result.ContactValidationResult.Failure
import ch.abwesend.privatecontacts.domain.model.result.batch.ContactBatchChangeResult
import ch.abwesend.privatecontacts.domain.model.result.batch.flattenedErrors
import ch.abwesend.privatecontacts.domain.repository.IAndroidContactSaveService
import ch.abwesend.privatecontacts.domain.repository.IContactRepository
import ch.abwesend.privatecontacts.domain.util.injectAnywhere

class ContactSaveService {
    private val validationService: ContactValidationService by injectAnywhere()
    private val sanitizingService: ContactSanitizingService by injectAnywhere()
    private val contactRepository: IContactRepository by injectAnywhere()
    private val androidContactService: IAndroidContactSaveService by injectAnywhere()

    suspend fun saveContact(contact: IContactEditable): ContactSaveResult {
        val validationResult = validationService.validateContact(contact)

        if (validationResult is Failure) {
            return ValidationFailure(validationResult.validationErrors)
        }
        sanitizingService.sanitizeContact(contact)

        return when (contact.type) {
            ContactType.SECRET -> saveContactInternally(contact)
            ContactType.PUBLIC -> saveContactExternally(contact)
        }
    }

    private suspend fun saveContactInternally(contact: IContactEditable): ContactSaveResult {
        val oldContactId = contact.id
        val newContactId = when (oldContactId) {
            is IContactIdInternal -> oldContactId
            is IContactIdExternal -> ContactIdInternal.randomId()
        }
        val contactIdChanged = newContactId != oldContactId

        if (contactIdChanged) {
            contact.changeContactDataIdsToInternal()
        }

        return if (contactIdChanged || contact.isNew) contactRepository.createContact(newContactId, contact)
        else contactRepository.updateContact(newContactId, contact)
    }

    private suspend fun saveContactExternally(contact: IContactEditable): ContactSaveResult {
        return when (val contactId = contact.id) {
            is IContactIdInternal -> {
                contact.changeContactDataIdsToExternal()
                // changing the contact from internal to external is treated as creating a new one
                androidContactService.createContact(contact)
            }
            is IContactIdExternal -> {
                if (contact.isNew) androidContactService.createContact(contact)
                else androidContactService.updateContact(contactId, contact)
            }
        }
    }

    private fun IContactEditable.changeContactDataIdsToInternal() =
        contactDataSet.replaceAll { contactData ->
            when (contactData.id) {
                is IContactDataIdInternal -> contactData
                is IContactDataIdExternal -> contactData.changeToInternalId()
            }
        }

    private fun IContactEditable.changeContactDataIdsToExternal() =
        contactDataSet.replaceAll { contactData ->
            when (contactData.id) {
                is IContactDataIdInternal -> contactData.changeToExternalId()
                is IContactDataIdExternal -> contactData
            }
        }

    suspend fun deleteContact(contact: IContactBase): ContactDeleteResult {
        val batchResult = deleteContacts(setOf(contact.id))
        return if (batchResult.completelySuccessful) ContactDeleteResult.Success
        else {
            val errors = batchResult.flattenedErrors().errors.distinct()
            val resultingError = errors.firstOrNull() ?: UNABLE_TO_DELETE_CONTACT
            ContactDeleteResult.Failure(resultingError)
        }
    }

    suspend fun deleteContacts(contactIds: Set<ContactId>): ContactBatchChangeResult {
        val internalContactIds = contactIds.filterIsInstance<IContactIdInternal>()
        val externalContactIds = contactIds.filterIsInstance<IContactIdExternal>()

        val internalResult = if (internalContactIds.isNotEmpty()) {
            contactRepository.deleteContacts(internalContactIds)
        } else ContactBatchChangeResult.empty()

        val externalResult = if (externalContactIds.isNotEmpty()) {
            androidContactService.deleteContacts(externalContactIds)
        } else ContactBatchChangeResult.empty()

        return internalResult.combine(externalResult)
    }
}
