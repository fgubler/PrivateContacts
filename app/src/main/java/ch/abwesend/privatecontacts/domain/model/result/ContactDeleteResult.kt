/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.result

sealed interface ContactDeleteResult {
    fun combine(other: ContactDeleteResult): ContactDeleteResult

    /** like a null-replacement: just does nothing */
    object Inactive : ContactDeleteResult {
        override fun combine(other: ContactDeleteResult): ContactDeleteResult = other
    }

    object Success : ContactDeleteResult {
        override fun combine(other: ContactDeleteResult): ContactDeleteResult =
            when (other) {
                is Success -> Success
                is Inactive -> this
                is Failure -> other
            }
    }

    @JvmInline
    value class Failure(val errors: List<ContactChangeError>) : ContactDeleteResult {
        constructor(error: ContactChangeError) : this(listOf(error))

        override fun combine(other: ContactDeleteResult): ContactDeleteResult = when (other) {
            is Success, is Inactive -> this
            is Failure -> Failure(errors = errors + other.errors)
        }
    }
}
