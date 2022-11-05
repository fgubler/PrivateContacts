/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.result

import ch.abwesend.privatecontacts.domain.model.result.ContactSaveResult.Failure
import ch.abwesend.privatecontacts.domain.model.result.ContactSaveResult.Success
import ch.abwesend.privatecontacts.domain.model.result.ContactSaveResult.ValidationFailure

// TODO merge with ContactDeleteResult
sealed interface ContactSaveResult {
    object Success : ContactSaveResult
    data class ValidationFailure(val validationErrors: List<ContactValidationError>) : ContactSaveResult

    /** "data class" because if a value class is used, some tests fail for some reason */
    data class Failure(val errors: List<ContactChangeError>) : ContactSaveResult {
        constructor(error: ContactChangeError) : this(listOf(error))
    }
}

val ContactSaveResult.errorsOrEmpty: List<ContactChangeError>
    get() = when (this) {
        is Failure -> errors
        is Success, is ValidationFailure -> emptyList()
    }

val ContactSaveResult.validationErrorsOrEmpty: List<ContactValidationError>
    get() = when (this) {
        is ValidationFailure -> validationErrors
        is Success, is Failure -> emptyList()
    }
