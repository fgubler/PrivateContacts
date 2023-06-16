/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.result.generic

data class SuccessResult<TValue, TError>(val value: TValue) : BinaryResult<TValue, TError> {
    override fun getValueOrNull(): TValue? = value
    override fun getErrorOrNull(): TError? = null

    override suspend fun <T> mapError(mapper: suspend (TError) -> T): BinaryResult<TValue, T> = SuccessResult(value)

    override suspend fun <T> mapValue(mapper: suspend (TValue) -> T): BinaryResult<T, TError> {
        val newValue = mapper(value)
        return SuccessResult(newValue)
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
