/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.service

import ch.abwesend.privatecontacts.domain.model.contact.IContact
import ch.abwesend.privatecontacts.domain.model.result.ContactValidationError
import ch.abwesend.privatecontacts.domain.model.result.ContactValidationError.NAME_NOT_SET
import ch.abwesend.privatecontacts.domain.model.result.ContactValidationResult

class ContactValidationService {
    suspend fun validateContact(contact: IContact): ContactValidationResult {
        val validationErrors = mutableListOf<ContactValidationError>()

        validationErrors.addAll(validateName(contact))

        return ContactValidationResult.fromErrors(validationErrors)
    }

    private fun validateName(contact: IContact): List<ContactValidationError> {
        val validationErrors = mutableListOf<ContactValidationError>()

        if (contact.firstName.trim().isEmpty() && contact.lastName.trim().isEmpty()) {
            validationErrors.add(NAME_NOT_SET)
        }

        return validationErrors
    }
}
