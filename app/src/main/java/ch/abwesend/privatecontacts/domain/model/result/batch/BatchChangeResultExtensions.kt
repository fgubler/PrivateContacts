package ch.abwesend.privatecontacts.domain.model.result.batch

/**
 * Flattens the partial error-objects into one.
 * The lists may contain duplicates: no [distinct] is executed to avoid changing the number of errors
 */
fun <T, TError, TValidationError> BatchChangeResultWithErrors<T, TError, TValidationError>.flattenedErrors():
    BatchChangeErrors<TError, TValidationError> {
    val errorObjects = failedChanges.map { it.value }
    val validationErrors = errorObjects.flatMap { it.validationErrors }
    val errors = errorObjects.flatMap { it.errors }
    return BatchChangeErrors(errors = errors, validationErrors = validationErrors)
}
