/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.contact

import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.service.interfaces.AccountService
import ch.abwesend.privatecontacts.domain.util.StringProvider

sealed interface ContactAccount {
    fun getDisplayName(stringProvider: StringProvider): String

    object AppInternal : ContactAccount {
        override fun getDisplayName(stringProvider: StringProvider): String =
            stringProvider(R.string.app_internal)
    }

    object LocalPhoneContacts : ContactAccount {
        override fun getDisplayName(stringProvider: StringProvider): String =
            stringProvider(R.string.local_phone_contacts)
    }

    /**
     * Account in which a contact can be stored.
     * Only relevant for new, external contacts
     */
    data class OnlineAccount(val username: String, val accountProvider: String) : ContactAccount {
        override fun getDisplayName(stringProvider: StringProvider): String =
            if (accountProvider == AccountService.ACCOUNT_PROVIDER_GOOGLE) "Google - $username"
            else "$accountProvider - $username" // TODO improve
    }
}