package ch.abwesend.privatecontacts.domain.model.result

data class BatchChangeErrors<TError, TValidationError>(
    val errors: List<TError>,
    val validationErrors: List<TValidationError>,
)

typealias ContactBatchChangeErrors = BatchChangeErrors<ContactChangeError, ContactValidationError>
