/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.result

sealed interface Result<TValue, TError> {
    fun getValueOrNull(): TValue?
    suspend fun <T> mapValue(mapper: suspend (TValue) -> T): Result<T, TError>
    suspend fun <T> mapError(mapper: suspend (TError) -> T): Result<TValue, T>
    suspend fun <T> mapValueToBinaryResult(
        mapper: suspend (TValue) -> BinaryResult<T, TError>
    ): BinaryResult<T, TError>

    fun ifHasValue(block: (TValue) -> Unit): Result<TValue, TError>
    fun ifHasError(block: (TError) -> Unit): Result<TValue, TError>

    data class Success<TValue, TError>(val value: TValue) : BinaryResult<TValue, TError> {
        override fun getValueOrNull(): TValue? = value
        override suspend fun <T> mapError(mapper: suspend (TError) -> T): BinaryResult<TValue, T> = Success(value)

        override suspend fun <T> mapValue(mapper: suspend (TValue) -> T): BinaryResult<T, TError> {
            val newValue = mapper(value)
            return Success(newValue)
        }

        override suspend fun <T> mapValueToBinaryResult(
            mapper: suspend (TValue) -> BinaryResult<T, TError>
        ): BinaryResult<T, TError> = mapper(value)

        override fun ifHasValue(block: (TValue) -> Unit): BinaryResult<TValue, TError> {
            block(value)
            return this
        }
        override fun ifHasError(block: (TError) -> Unit) = this
    }

    data class Error<TValue, TError>(val error: TError) : BinaryResult<TValue, TError> {
        override fun getValueOrNull(): TValue? = null
        override suspend fun <T> mapValue(mapper: suspend (TValue) -> T): BinaryResult<T, TError> = Error(error)
        override suspend fun <T> mapError(mapper: suspend (TError) -> T): BinaryResult<TValue, T> {
            val newError = mapper(error)
            return Error(newError)
        }
        override suspend fun <T> mapValueToBinaryResult(
            mapper: suspend (TValue) -> BinaryResult<T, TError>
        ): BinaryResult<T, TError> = Error(error)

        override fun ifHasValue(block: (TValue) -> Unit) = this
        override fun ifHasError(block: (TError) -> Unit): BinaryResult<TValue, TError> {
            block(error)
            return this
        }
    }
}

sealed interface BinaryResult<TValue, TError> : Result<TValue, TError> {
    override suspend fun <T> mapValue(mapper: suspend (TValue) -> T): BinaryResult<T, TError>
    override suspend fun <T> mapError(mapper: suspend (TError) -> T): BinaryResult<TValue, T>

    override fun ifHasValue(block: (TValue) -> Unit): BinaryResult<TValue, TError>
    override fun ifHasError(block: (TError) -> Unit): BinaryResult<TValue, TError>
}
