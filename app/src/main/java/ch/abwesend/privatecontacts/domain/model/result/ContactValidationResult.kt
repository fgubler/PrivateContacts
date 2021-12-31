package ch.abwesend.privatecontacts.domain.model.result

sealed interface ContactValidationResult {
    object Success : ContactValidationResult
    data class Failure(val validationErrors: List<ContactValidationError>) : ContactValidationResult

    companion object {
        fun fromErrors(validationErrors: List<ContactValidationError>): ContactValidationResult =
            if (validationErrors.isEmpty()) Success
            else Failure(validationErrors)
    }
}

enum class ContactValidationError {
    NAME_NOT_SET
}
