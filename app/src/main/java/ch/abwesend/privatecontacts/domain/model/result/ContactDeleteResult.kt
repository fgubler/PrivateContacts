/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.result

sealed interface ContactDeleteResult {
    fun combine(other: ContactDeleteResult): ContactDeleteResult

    object Success : ContactDeleteResult {
        override fun combine(other: ContactDeleteResult): ContactDeleteResult =
            when (other) {
                is Success -> Success
                is Failure -> other
            }
    }

    @JvmInline
    value class Failure(val errors: List<ContactChangeError>) : ContactDeleteResult {
        constructor(error: ContactChangeError) : this(listOf(error))

        override fun combine(other: ContactDeleteResult): ContactDeleteResult = when (other) {
            is Success -> this
            is Failure -> Failure(errors = errors + other.errors)
        }
    }
}
