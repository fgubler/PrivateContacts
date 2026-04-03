/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.result.generic

data class SuccessResult<TValue>(val value: TValue) : BinaryResult<TValue, Nothing> {
    override fun getValueOrNull(): TValue? = value
    override fun getErrorOrNull(): Nothing? = null

    override suspend fun <T> mapError(mapper: suspend (Nothing) -> T): BinaryResult<TValue, T> = SuccessResult(value)

    override suspend fun <T> mapValue(mapper: suspend (TValue) -> T): BinaryResult<T, Nothing> {
        val newValue = mapper(value)
        return SuccessResult(newValue)
    }

    override suspend fun ifHasValue(block: suspend (TValue) -> Unit): BinaryResult<TValue, Nothing> {
        block(value)
        return this
    }
    override suspend fun ifHasError(block: suspend (Nothing) -> Unit) = this
}
