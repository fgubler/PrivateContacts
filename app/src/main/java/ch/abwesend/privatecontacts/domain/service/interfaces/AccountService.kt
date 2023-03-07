/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.service.interfaces

import ch.abwesend.privatecontacts.domain.model.contact.ContactAccount

interface AccountService {
    /**
     * @return a list of the available accounts.
     * Will never (!) be empty: at least the local-phone-contacts will be there.
     */
    fun loadAvailableAccounts(): List<ContactAccount>

    companion object {
        const val ACCOUNT_PROVIDER_GOOGLE = "com.google"
    }
}
