/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.service

import ch.abwesend.privatecontacts.domain.model.contact.IContact
import ch.abwesend.privatecontacts.domain.model.result.ContactSaveResult
import ch.abwesend.privatecontacts.domain.model.result.ContactSaveResult.ValidationFailure
import ch.abwesend.privatecontacts.domain.model.result.ContactValidationResult.Failure
import ch.abwesend.privatecontacts.domain.repository.IContactRepository
import ch.abwesend.privatecontacts.domain.util.injectAnywhere

class ContactSaveService {
    private val validationService: ContactValidationService by injectAnywhere()
    private val contactRepository: IContactRepository by injectAnywhere()

    suspend fun saveContact(contact: IContact): ContactSaveResult {
        val validationResult = validationService.validateContact(contact)

        return when {
            validationResult is Failure -> ValidationFailure(validationResult.validationErrors)
            contact.isNew -> contactRepository.createContact(contact)
            else -> contactRepository.updateContact(contact)
        }
    }
}
