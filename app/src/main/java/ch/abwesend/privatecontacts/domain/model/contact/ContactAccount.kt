/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.contact

import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.service.interfaces.AccountService
import ch.abwesend.privatecontacts.domain.settings.Settings
import ch.abwesend.privatecontacts.domain.util.StringProvider

sealed interface ContactAccount {
    val type: ContactAccountType
    fun getDisplayName(stringProvider: StringProvider): String

    object None : ContactAccount {
        override val type: ContactAccountType = ContactAccountType.NONE
        override fun getDisplayName(stringProvider: StringProvider): String =
            stringProvider(R.string.app_internal)
    }

    object LocalPhoneContacts : ContactAccount {
        override val type: ContactAccountType = ContactAccountType.LOCAL_PHONE_CONTACTS
        override fun getDisplayName(stringProvider: StringProvider): String =
            stringProvider(R.string.local_phone_contacts)
    }

    /**
     * Account in which a contact can be stored.
     * Only relevant for new, external contacts
     */
    data class OnlineAccount(val username: String, val accountProvider: String) : ContactAccount {
        override val type: ContactAccountType = ContactAccountType.ONLINE_ACCOUNT
        override fun getDisplayName(stringProvider: StringProvider): String =
            if (accountProvider == AccountService.ACCOUNT_PROVIDER_GOOGLE) "Google - $username"
            else "$accountProvider - $username" // TODO improve
    }

    companion object {
        val defaultForExternal: ContactAccount = LocalPhoneContacts
        fun currentDefaultForContactType(contactType: ContactType): ContactAccount =
            when (contactType) {
                ContactType.SECRET -> None
                ContactType.PUBLIC -> Settings.current.defaultExternalContactAccount
            }
    }
}

/** mostly needed for serialization */
enum class ContactAccountType {
    NONE,
    LOCAL_PHONE_CONTACTS,
    ONLINE_ACCOUNT,
}

val ContactAccount.usernameOrNull: String?
    get() = (this as? ContactAccount.OnlineAccount)?.username

val ContactAccount.accountProviderOrNull: String?
    get() = (this as? ContactAccount.OnlineAccount)?.accountProvider
