package ch.abwesend.privatecontacts.domain.service

import ch.abwesend.privatecontacts.domain.model.contact.Contact
import ch.abwesend.privatecontacts.domain.model.contact.getFullName
import ch.abwesend.privatecontacts.domain.model.result.ContactValidationError
import ch.abwesend.privatecontacts.domain.model.result.ContactValidationError.NAME_NOT_SET
import ch.abwesend.privatecontacts.domain.model.result.ContactValidationResult

class ContactValidationService {
    suspend fun validateContact(contact: Contact): ContactValidationResult {
        val validationErrors = mutableListOf<ContactValidationError>()

        validationErrors.addAll(validateName(contact))

        return ContactValidationResult.fromErrors(validationErrors)
    }

    private fun validateName(contact: Contact): List<ContactValidationError> {
        val validationErrors = mutableListOf<ContactValidationError>()

        if (contact.getFullName(true).trim().isEmpty()) {
            validationErrors.add(NAME_NOT_SET)
        }

        return validationErrors
    }
}
