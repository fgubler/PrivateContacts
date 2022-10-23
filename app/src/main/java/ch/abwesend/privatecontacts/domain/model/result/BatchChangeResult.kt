/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.result

import ch.abwesend.privatecontacts.domain.model.contact.ContactId

data class BatchChangeResult<T>(
    val successfulChanges: List<T>,
    val failedChanges: List<T>,
) {
    val completelySuccessful: Boolean
        get() = successfulChanges.isNotEmpty() && failedChanges.isEmpty()

    val completelyFailed: Boolean
        get() = successfulChanges.isEmpty() && failedChanges.isNotEmpty()

    val isEmpty: Boolean
        get() = successfulChanges.isEmpty() && failedChanges.isEmpty()

    fun combine(other: BatchChangeResult<T>): BatchChangeResult<T> = BatchChangeResult(
        successfulChanges = successfulChanges + other.successfulChanges,
        failedChanges = failedChanges + other.failedChanges,
    )

    companion object {
        fun <T> success(changes: List<T>): BatchChangeResult<T> = BatchChangeResult(
            successfulChanges = changes,
            failedChanges = emptyList(),
        )
        fun <T> failure(changes: List<T>): BatchChangeResult<T> = BatchChangeResult(
            successfulChanges = emptyList(),
            failedChanges = changes,
        )
        fun <T> empty(): BatchChangeResult<T> = BatchChangeResult(emptyList(), emptyList())
    }
}

typealias ContactBatchChangeResult = BatchChangeResult<ContactId>
