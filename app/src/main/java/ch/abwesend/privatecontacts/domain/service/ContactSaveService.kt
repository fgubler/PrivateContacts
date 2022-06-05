/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.service

import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.contact.IContactBase
import ch.abwesend.privatecontacts.domain.model.contact.IContactEditable
import ch.abwesend.privatecontacts.domain.model.contact.IContactId
import ch.abwesend.privatecontacts.domain.model.contact.IContactIdExternal
import ch.abwesend.privatecontacts.domain.model.contact.IContactIdInternal
import ch.abwesend.privatecontacts.domain.model.result.ContactDeleteResult
import ch.abwesend.privatecontacts.domain.model.result.ContactSaveResult
import ch.abwesend.privatecontacts.domain.model.result.ContactSaveResult.ValidationFailure
import ch.abwesend.privatecontacts.domain.model.result.ContactValidationResult.Failure
import ch.abwesend.privatecontacts.domain.repository.IContactRepository
import ch.abwesend.privatecontacts.domain.util.injectAnywhere

class ContactSaveService {
    private val validationService: ContactValidationService by injectAnywhere()
    private val sanitizingService: ContactSanitizingService by injectAnywhere()
    private val contactRepository: IContactRepository by injectAnywhere()

    suspend fun saveContact(contact: IContactEditable): ContactSaveResult {
        val validationResult = validationService.validateContact(contact)

        if (validationResult is Failure) {
            return ValidationFailure(validationResult.validationErrors)
        }
        sanitizingService.sanitizeContact(contact)

        return if (contact.id is IContactIdInternal) {
            if (contact.isNew) contactRepository.createContact(contact)
            else contactRepository.updateContact(contact)
        } else TODO("Saving android contacts is not yet implemented")
    }

    suspend fun deleteContact(contact: IContactBase): ContactDeleteResult =
        deleteContacts(setOf(contact.id))

    suspend fun deleteContacts(contactIds: Set<IContactId>): ContactDeleteResult {
        val internalContactIds = contactIds.filterIsInstance<IContactIdInternal>()
        val externalContactIds = contactIds.filterIsInstance<IContactIdExternal>()

        val internalResult = if (internalContactIds.isNotEmpty()) {
            contactRepository.deleteContacts(internalContactIds)
        } else ContactDeleteResult.Inactive

        val externalResult = if (externalContactIds.isNotEmpty()) {
            logger.warning("Tried to delete android contacts but that is not yet supported")
            ContactDeleteResult.Failure(emptyList())
            // TODO implement
        } else ContactDeleteResult.Inactive

        return internalResult.combine(externalResult)
    }
}
