/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.result

sealed interface Result<TValue, TError> {
    fun getValueOrNull(): TValue?
    fun <T> mapValue(mapper: (TValue) -> T): Result<T, TError>
    suspend fun <T> mapValueSuspending(mapper: suspend (TValue) -> T): Result<T, TError>

    data class Success<TValue, TError>(val value: TValue) : BinaryResult<TValue, TError> {
        override fun getValueOrNull(): TValue? = value

        override fun <T> mapValue(mapper: (TValue) -> T): Result<T, TError> {
            val newValue = mapper(value)
            return Success(newValue)
        }

        override suspend fun <T> mapValueSuspending(mapper: suspend (TValue) -> T): Result<T, TError> {
            val newValue = mapper(value)
            return Success(newValue)
        }
    }

    data class Error<TValue, TError>(val error: TError) : BinaryResult<TValue, TError> {
        override fun getValueOrNull(): TValue? = null
        override fun <T> mapValue(mapper: (TValue) -> T): Result<T, TError> = Error(error)
        override suspend fun <T> mapValueSuspending(mapper: suspend (TValue) -> T): Result<T, TError> = Error(error)
    }
}

sealed interface BinaryResult<TValue, TError> : Result<TValue, TError>
