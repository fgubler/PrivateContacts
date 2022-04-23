/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.service

import ch.abwesend.privatecontacts.domain.model.contact.IContactBase
import ch.abwesend.privatecontacts.domain.model.contact.IContactEditable
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

        return if (contact.isNew) contactRepository.createContact(contact)
        else contactRepository.updateContact(contact)
    }

    suspend fun deleteContact(contact: IContactBase): ContactDeleteResult =
        contactRepository.deleteContact(contact)
}
