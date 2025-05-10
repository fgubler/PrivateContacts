/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.result.generic

sealed interface Result<TValue, TError> {
    fun getValueOrNull(): TValue?
    fun getErrorOrNull(): TError?
    suspend fun <T> mapValue(mapper: suspend (TValue) -> T): Result<T, TError>
    suspend fun <T> mapError(mapper: suspend (TError) -> T): Result<TValue, T>
    suspend fun <T> mapValueToBinaryResult(
        mapper: suspend (TValue) -> BinaryResult<T, TError>
    ): BinaryResult<T, TError>

    suspend fun ifHasValue(block: suspend (TValue) -> Unit): Result<TValue, TError>
    suspend fun ifHasError(block: suspend (TError) -> Unit): Result<TValue, TError>
}

sealed interface BinaryResult<TValue, TError> : Result<TValue, TError> {
    override suspend fun <T> mapValue(mapper: suspend (TValue) -> T): BinaryResult<T, TError>
    override suspend fun <T> mapError(mapper: suspend (TError) -> T): BinaryResult<TValue, T>

    override suspend fun ifHasValue(block: suspend (TValue) -> Unit): BinaryResult<TValue, TError>
    override suspend fun ifHasError(block: suspend (TError) -> Unit): BinaryResult<TValue, TError>
}
