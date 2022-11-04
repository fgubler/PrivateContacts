package ch.abwesend.privatecontacts.domain.model.result.batch

import ch.abwesend.privatecontacts.domain.model.result.ContactChangeError
import ch.abwesend.privatecontacts.domain.model.result.ContactValidationError

data class BatchChangeErrors<TError, TValidationError>(
    val errors: List<TError>,
    val validationErrors: List<TValidationError>,
)

typealias ContactBatchChangeErrors = BatchChangeErrors<ContactChangeError, ContactValidationError>
