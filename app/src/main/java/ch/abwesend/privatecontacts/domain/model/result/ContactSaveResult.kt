package ch.abwesend.privatecontacts.domain.model.result

import androidx.annotation.StringRes
import ch.abwesend.privatecontacts.R

sealed interface ContactSaveResult {
    object Success : ContactSaveResult
    data class ValidationFailure(val validationErrors: List<ContactValidationError>) : ContactSaveResult

    @JvmInline
    value class Failure(val error: ContactSavingError) : ContactSaveResult
}

enum class ContactSavingError(@StringRes val label: Int) {
    UNKNOWN_ERROR(R.string.unknown_error)
}
