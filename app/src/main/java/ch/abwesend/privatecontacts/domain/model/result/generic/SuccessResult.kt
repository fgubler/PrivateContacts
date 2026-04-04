/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.result.generic

data class SuccessResult<TValue>(val value: TValue) : BinaryResult<TValue, Nothing> {
    override fun getValueOrNull(): TValue? = value
    override fun getErrorOrNull(): Nothing? = null
}
