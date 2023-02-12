/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.contact

const val ACCOUNT_PROVIDER_GOOGLE = "com.google"

/**
 * Account in which a contact can be stored.
 * Only relevant for new, external contacts
 */
data class ContactAccount(val username: String, val accountProvider: String) {
    val displayName: String
        get() =
            if (accountProvider == ACCOUNT_PROVIDER_GOOGLE) "Google - $username"
            else "$accountProvider - $username" // TODO improve
}