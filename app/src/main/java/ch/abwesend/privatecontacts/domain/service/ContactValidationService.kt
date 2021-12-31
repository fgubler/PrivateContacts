package ch.abwesend.privatecontacts.domain.service

import ch.abwesend.privatecontacts.domain.model.contact.Contact
import ch.abwesend.privatecontacts.domain.model.contact.getFullName
import ch.abwesend.privatecontacts.domain.model.result.ContactValidationError
import ch.abwesend.privatecontacts.domain.model.result.ContactValidationResult

interface IContactValidationService {
    suspend fun validateContact(contact: Contact): ContactValidationResult
}

class ContactValidationService : IContactValidationService {
    override suspend fun validateContact(contact: Contact): ContactValidationResult {
        val validationErrors = mutableListOf<ContactValidationError>()

        if (contact.getFullName(true).trim().isEmpty()) {
            validationErrors.add(ContactValidationError.NAME_NOT_SET)
        }

        return ContactValidationResult.fromErrors(validationErrors)
    }
}
