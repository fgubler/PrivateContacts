/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.service

import ch.abwesend.privatecontacts.domain.model.contact.IContact
import ch.abwesend.privatecontacts.domain.model.contact.IContactBase
import ch.abwesend.privatecontacts.domain.model.result.ContactValidationError
import ch.abwesend.privatecontacts.domain.model.result.ContactValidationError.NAME_NOT_SET
import ch.abwesend.privatecontacts.domain.model.result.ContactValidationResult

class ContactValidationService {
    fun validateContact(contact: IContact): ContactValidationResult {
        val validationErrors = mutableListOf<ContactValidationError>()

        validationErrors.addAll(contact.validateName())

        return ContactValidationResult.fromErrors(validationErrors)
    }

    fun validateContactBase(contact: IContactBase): List<ContactValidationError> =
        contact.validateName()
}

private fun IContactBase.validateName(): List<ContactValidationError> {
    val validationErrors = mutableListOf<ContactValidationError>()

    if (displayName.trim().isEmpty()) {
        validationErrors.add(NAME_NOT_SET)
    }

    return validationErrors
}

val List<ContactValidationError>.valid: Boolean
    get() = isEmpty()
