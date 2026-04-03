/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.result.generic

sealed interface Result<out TValue, out TError> {
    fun getValueOrNull(): TValue?
    fun getErrorOrNull(): TError?
    suspend fun <T> mapValue(mapper: suspend (TValue) -> T): Result<T, TError>
    suspend fun <T> mapError(mapper: suspend (TError) -> T): Result<TValue, T>

    suspend fun ifHasValue(block: suspend (TValue) -> Unit): Result<TValue, TError>
    suspend fun ifHasError(block: suspend (TError) -> Unit): Result<TValue, TError>
}

sealed interface BinaryResult<out TValue, out TError> : Result<TValue, TError> {
    override suspend fun <T> mapValue(mapper: suspend (TValue) -> T): BinaryResult<T, TError>
    override suspend fun <T> mapError(mapper: suspend (TError) -> T): BinaryResult<TValue, T>

    override suspend fun ifHasValue(block: suspend (TValue) -> Unit): BinaryResult<TValue, TError>
    override suspend fun ifHasError(block: suspend (TError) -> Unit): BinaryResult<TValue, TError>
}
