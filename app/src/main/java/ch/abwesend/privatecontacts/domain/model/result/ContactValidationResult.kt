/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.result

import androidx.annotation.StringRes
import ch.abwesend.privatecontacts.R

sealed interface ContactValidationResult {
    object Success : ContactValidationResult
    data class Failure(val validationErrors: List<ContactValidationError>) : ContactValidationResult

    companion object {
        fun fromErrors(validationErrors: List<ContactValidationError>): ContactValidationResult =
            if (validationErrors.isEmpty()) Success
            else Failure(validationErrors)
    }
}

enum class ContactValidationError(@StringRes val label: Int) {
    NAME_NOT_SET(R.string.contact_validation_error_name_empty)
}
