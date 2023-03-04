package ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.factory

import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.contact.ContactAccount
import ch.abwesend.privatecontacts.domain.model.contact.ContactAccount.LocalPhoneContacts
import ch.abwesend.privatecontacts.domain.model.contact.ContactAccount.None
import ch.abwesend.privatecontacts.domain.model.contact.ContactAccount.OnlineAccount
import com.alexstyl.contactstore.InternetAccount

fun ContactAccount.toInternetAccountOrNull(): InternetAccount? =
    when (this) {
        is None -> null.also {
            logger.warning("invalid account-type 'internal' for external contact ")
        }
        is LocalPhoneContacts -> null
        is OnlineAccount -> toInternetAccount()
    }

fun OnlineAccount.toInternetAccount(): InternetAccount =
    InternetAccount(name = username, type = accountProvider)

fun InternetAccount.toContactAccount(): OnlineAccount =
    OnlineAccount(username = name, accountProvider = type)
