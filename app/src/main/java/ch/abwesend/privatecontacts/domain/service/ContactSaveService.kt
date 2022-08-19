/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.service

import ch.abwesend.privatecontacts.domain.model.contact.ContactId
import ch.abwesend.privatecontacts.domain.model.contact.ContactIdInternal
import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.model.contact.IContact
import ch.abwesend.privatecontacts.domain.model.contact.IContactBase
import ch.abwesend.privatecontacts.domain.model.contact.IContactEditable
import ch.abwesend.privatecontacts.domain.model.contact.IContactIdExternal
import ch.abwesend.privatecontacts.domain.model.contact.IContactIdInternal
import ch.abwesend.privatecontacts.domain.model.result.ContactChangeError.NOT_YET_IMPLEMENTED_FOR_EXTERNAL_CONTACTS
import ch.abwesend.privatecontacts.domain.model.result.ContactDeleteResult
import ch.abwesend.privatecontacts.domain.model.result.ContactSaveResult
import ch.abwesend.privatecontacts.domain.model.result.ContactSaveResult.ValidationFailure
import ch.abwesend.privatecontacts.domain.model.result.ContactValidationResult.Failure
import ch.abwesend.privatecontacts.domain.repository.IAndroidContactSaveRepository
import ch.abwesend.privatecontacts.domain.repository.IContactRepository
import ch.abwesend.privatecontacts.domain.util.injectAnywhere

class ContactSaveService {
    private val validationService: ContactValidationService by injectAnywhere()
    private val sanitizingService: ContactSanitizingService by injectAnywhere()
    private val contactRepository: IContactRepository by injectAnywhere()
    private val androidContactRepository: IAndroidContactSaveRepository by injectAnywhere()

    suspend fun saveContact(contact: IContactEditable): ContactSaveResult {
        val validationResult = validationService.validateContact(contact)

        if (validationResult is Failure) {
            return ValidationFailure(validationResult.validationErrors)
        }
        sanitizingService.sanitizeContact(contact)

        return when (contact.type) {
            ContactType.SECRET -> saveContactInternally(contact)
            ContactType.PUBLIC -> ContactSaveResult.Failure(NOT_YET_IMPLEMENTED_FOR_EXTERNAL_CONTACTS)
        }
    }

    private suspend fun saveContactInternally(contact: IContact): ContactSaveResult {
        val oldContactId = contact.id
        val newContactId = when (oldContactId) {
            is IContactIdInternal -> oldContactId
            is IContactIdExternal -> ContactIdInternal.randomId()
        }
        val contactIdChanged = newContactId != oldContactId

        return if (contactIdChanged || contact.isNew) contactRepository.createContact(newContactId, contact)
        else contactRepository.updateContact(newContactId, contact)
    }

    suspend fun deleteContact(contact: IContactBase): ContactDeleteResult =
        deleteContacts(setOf(contact.id))

    suspend fun deleteContacts(contactIds: Set<ContactId>): ContactDeleteResult {
        val internalContactIds = contactIds.filterIsInstance<IContactIdInternal>()
        val externalContactIds = contactIds.filterIsInstance<IContactIdExternal>()

        val internalResult = if (internalContactIds.isNotEmpty()) {
            contactRepository.deleteContacts(internalContactIds)
        } else ContactDeleteResult.Inactive

        val externalResult = if (externalContactIds.isNotEmpty()) {
            androidContactRepository.deleteContacts(externalContactIds)
        } else ContactDeleteResult.Inactive

        return internalResult.combine(externalResult)
    }
}
