/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.result.generic

data class ErrorResult<TError>(val error: TError) : BinaryResult<Nothing, TError> {
    override fun getValueOrNull(): Nothing? = null
    override fun getErrorOrNull(): TError = error
}
