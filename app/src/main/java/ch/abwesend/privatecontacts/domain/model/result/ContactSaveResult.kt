/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.result

sealed interface ContactSaveResult {
    object Success : ContactSaveResult
    data class ValidationFailure(val validationErrors: List<ContactValidationError>) : ContactSaveResult

    @JvmInline
    value class Failure(val error: ContactChangeError) : ContactSaveResult
}
