/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.result

sealed interface ContactDeleteResult {
    object Success : ContactDeleteResult

    @JvmInline
    value class Failure(val error: ContactChangeError) : ContactDeleteResult
}