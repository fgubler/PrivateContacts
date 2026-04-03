/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.result.generic

sealed interface BinaryResult<out TValue, out TError> {
    fun getValueOrNull(): TValue?
    fun getErrorOrNull(): TError?
    suspend fun <T> mapValue(mapper: suspend (TValue) -> T): BinaryResult<T, TError>
    suspend fun <T> mapError(mapper: suspend (TError) -> T): BinaryResult<TValue, T>

    suspend fun ifHasValue(block: suspend (TValue) -> Unit): BinaryResult<TValue, TError>
    suspend fun ifHasError(block: suspend (TError) -> Unit): BinaryResult<TValue, TError>
}
