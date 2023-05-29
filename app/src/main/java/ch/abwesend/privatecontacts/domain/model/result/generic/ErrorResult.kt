/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.result.generic

data class ErrorResult<TValue, TError>(val error: TError) : BinaryResult<TValue, TError> {
    override fun getValueOrNull(): TValue? = null
    override suspend fun <T> mapValue(mapper: suspend (TValue) -> T): BinaryResult<T, TError> = ErrorResult(error)
    override suspend fun <T> mapError(mapper: suspend (TError) -> T): BinaryResult<TValue, T> {
        val newError = mapper(error)
        return ErrorResult(newError)
    }
    override suspend fun <T> mapValueToBinaryResult(
        mapper: suspend (TValue) -> BinaryResult<T, TError>
    ): BinaryResult<T, TError> = ErrorResult(error)

    override fun ifHasValue(block: (TValue) -> Unit) = this
    override fun ifHasError(block: (TError) -> Unit): BinaryResult<TValue, TError> {
        block(error)
        return this
    }
}
