/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.result

sealed interface ContactSaveResult {
    object Success : ContactSaveResult
    data class ValidationFailure(val validationErrors: List<ContactValidationError>) : ContactSaveResult

    /** if a value class is used, some tests fail for some reason */
    data class Failure(val errors: List<ContactChangeError>) : ContactSaveResult {
        constructor(error: ContactChangeError) : this(listOf(error))
    }
}
