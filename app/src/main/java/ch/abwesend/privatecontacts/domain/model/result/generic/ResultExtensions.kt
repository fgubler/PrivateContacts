/*
 * Private Contacts
 * Copyright (c) 2026.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.result.generic

import kotlinx.coroutines.CancellationException

inline fun <T> runCatchingAsResult(block: () -> T): BinaryResult<T, Exception> {
    return try {
        SuccessResult(block())
    } catch (e: CancellationException) {
        throw e // do not catch coroutine-cancellations
    } catch (e: Exception) {
        ErrorResult(e)
    }
}

inline fun <T> runCatchingOnResult(block: () -> BinaryResult<T, Exception>): BinaryResult<T, Exception> {
    return try {
        block()
    } catch (e: CancellationException) {
        throw e // do not catch coroutine-cancellations
    } catch (e: Exception) {
        ErrorResult(e)
    }
}

inline fun <TValue, TError, T> BinaryResult<TValue, TError>.mapValue(
    mapper: (TValue) -> T
): BinaryResult<T, TError> = when (this) {
    is ErrorResult -> ErrorResult(error)
    is SuccessResult -> SuccessResult(mapper(value))
}

inline fun <TValue, TError, T> BinaryResult<TValue, TError>.mapError(
    mapper: (TError) -> T
): BinaryResult<TValue, T> = when (this) {
    is SuccessResult -> SuccessResult(value)
    is ErrorResult -> ErrorResult(mapper(error))
}

inline fun <TValue, TError, T> BinaryResult<TValue, TError>.mapValueToResult(
    mapper: (TValue) -> BinaryResult<T, TError>
): BinaryResult<T, TError> = when (this) {
    is ErrorResult -> this
    is SuccessResult -> mapper(value)
}

inline fun <TValue, TError> BinaryResult<TValue, TError>.ifSuccess(
    block: (TValue) -> Unit
): BinaryResult<TValue, TError> {
    when (this) {
        is SuccessResult -> block(value)
        is ErrorResult -> Unit
    }
    return this
}

inline fun <TValue, TError> BinaryResult<TValue, TError>.ifError(
    block: (TError) -> Unit
): BinaryResult<TValue, TError> {
    when (this) {
        is ErrorResult -> block(error)
        is SuccessResult -> Unit
    }
    return this
}
