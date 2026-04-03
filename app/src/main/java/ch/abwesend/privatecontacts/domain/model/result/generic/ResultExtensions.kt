/*
 * Private Contacts
 * Copyright (c) 2026.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.result.generic

inline fun <T> runCatchingAsResult(block: () -> T): BinaryResult<T, Exception> {
    return try {
        SuccessResult(block())
    } catch (e: Exception) {
        ErrorResult(e)
    }
}

suspend inline fun <TValue, TError, T> Result<TValue, TError>.mapValueToBinaryResult(
    mapper: suspend (TValue) -> BinaryResult<T, TError>
): BinaryResult<T, TError> = when (this) {
    is ErrorResult -> this
    is SuccessResult -> mapper(value)
}