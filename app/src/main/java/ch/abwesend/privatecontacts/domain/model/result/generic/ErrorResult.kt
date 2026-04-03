/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.result.generic

data class ErrorResult<TError>(val error: TError) : BinaryResult<Nothing, TError> {
    override fun getValueOrNull(): Nothing? = null
    override fun getErrorOrNull(): TError = error

    override suspend fun <T> mapValue(mapper: suspend (Nothing) -> T): BinaryResult<T, TError> = ErrorResult(error)
    override suspend fun <T> mapError(mapper: suspend (TError) -> T): BinaryResult<Nothing, T> {
        val newError = mapper(error)
        return ErrorResult(newError)
    }

    override suspend fun ifHasValue(block: suspend (Nothing) -> Unit) = this
    override suspend fun ifHasError(block: suspend (TError) -> Unit): BinaryResult<Nothing, TError> {
        block(error)
        return this
    }
}
