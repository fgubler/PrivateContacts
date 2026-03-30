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
