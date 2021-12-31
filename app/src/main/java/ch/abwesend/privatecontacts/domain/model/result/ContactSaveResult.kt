package ch.abwesend.privatecontacts.domain.model.result

sealed interface ContactSaveResult {
    object Success : ContactSaveResult
    data class ValidationFailure(val validationErrors: List<ContactValidationError>) : ContactSaveResult

    @JvmInline
    value class Failure(val error: SavingError) : ContactSaveResult
}

enum class SavingError {
    UNKNOWN_ERROR
}
