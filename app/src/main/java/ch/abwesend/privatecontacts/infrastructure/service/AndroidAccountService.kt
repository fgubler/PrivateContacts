/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.service

import android.accounts.AccountManager
import android.content.Context
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.contact.ContactAccount
import ch.abwesend.privatecontacts.domain.model.contact.ContactAccount.OnlineAccount
import ch.abwesend.privatecontacts.domain.service.interfaces.AccountService
import ch.abwesend.privatecontacts.domain.service.interfaces.AccountService.Companion.ACCOUNT_PROVIDER_GOOGLE
import ch.abwesend.privatecontacts.domain.service.interfaces.PermissionService
import ch.abwesend.privatecontacts.domain.util.injectAnywhere

class AndroidAccountService(private val context: Context) : AccountService {
    private val permissionService: PermissionService by injectAnywhere()

    private val knownAccountProviders: Set<String> = setOf(ACCOUNT_PROVIDER_GOOGLE) // TODO extend

    override fun loadAvailableAccounts(showThirdPartyAccounts: Boolean): List<ContactAccount> {
        val onlineAccounts: List<ContactAccount> = if (!permissionService.hasReadAccountsPermission()) {
            logger.warning("Missing permission to load accounts: cannot suggest more than local-contacts.")
            emptyList()
        } else {
            val accounts = AccountManager.get(context).accounts

            logger.debug(
                "Loading accounts, ${if (showThirdPartyAccounts) "including" else "excluding"} third-party ones."
            )

            accounts
                .map { OnlineAccount(username = it.name, accountProvider = it.type) }
                .also { logger.debug("Found ${it.size} accounts") }
                .filter { showThirdPartyAccounts || knownAccountProviders.contains(it.accountProvider) }
                .also { logger.debug("Found ${it.size} accounts of known providers") }
                .sortedBy { it.username } // just to make the order constant
        }
        return onlineAccounts + ContactAccount.LocalPhoneContacts
    }
}
