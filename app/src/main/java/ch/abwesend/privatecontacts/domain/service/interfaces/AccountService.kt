/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.service.interfaces

import ch.abwesend.privatecontacts.domain.model.contact.ContactAccount


interface AccountService {
    fun loadAvailableAccounts(): List<ContactAccount>

    companion object {
        const val ACCOUNT_PROVIDER_GOOGLE = "com.google"
    }
}
