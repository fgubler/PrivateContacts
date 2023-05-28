/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.result.batch

import ch.abwesend.privatecontacts.domain.model.contact.ContactId

data class BatchChangeResult<T, TError>(
    val successfulChanges: List<T>,
    val failedChanges: Map<T, TError>,
) {
    val completelySuccessful: Boolean
        get() = successfulChanges.isNotEmpty() && failedChanges.isEmpty()

    val completelyFailed: Boolean
        get() = successfulChanges.isEmpty() && failedChanges.isNotEmpty()

    val isEmpty: Boolean
        get() = successfulChanges.isEmpty() && failedChanges.isEmpty()

    val numberOfAttemptedChanges: Int
        get() = successfulChanges.size + failedChanges.size

    fun combine(other: BatchChangeResult<T, TError>): BatchChangeResult<T, TError> = BatchChangeResult(
        successfulChanges = successfulChanges + other.successfulChanges,
        failedChanges = failedChanges + other.failedChanges,
    )

    companion object {
        fun <T, TError> success(changes: List<T>): BatchChangeResult<T, TError> = BatchChangeResult(
            successfulChanges = changes,
            failedChanges = emptyMap(),
        )
        fun <T, TError> failure(changes: Map<T, TError>): BatchChangeResult<T, TError> = BatchChangeResult(
            successfulChanges = emptyList(),
            failedChanges = changes,
        )
        fun <T, TError> empty(): BatchChangeResult<T, TError> = BatchChangeResult(emptyList(), emptyMap())
    }
}

typealias ContactIdBatchChangeResult = BatchChangeResult<ContactId, ContactBatchChangeErrors>
typealias BatchChangeResultWithErrors<T, TError, TValidationError> =
    BatchChangeResult<T, BatchChangeErrors<TError, TValidationError>>
