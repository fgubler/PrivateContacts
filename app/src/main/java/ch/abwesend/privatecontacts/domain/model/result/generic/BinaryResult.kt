/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.result.generic

/**
 * Construct to represent either a successful result or an error.
 * See also ResultExtensions.kt for additional methods: extension-methods have the advantage of inlining.
 */
sealed interface BinaryResult<out TValue, out TError> {
    fun getValueOrNull(): TValue?
    fun getErrorOrNull(): TError?
}
