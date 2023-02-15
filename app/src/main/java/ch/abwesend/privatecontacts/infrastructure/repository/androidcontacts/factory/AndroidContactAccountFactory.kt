package ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.factory

import ch.abwesend.privatecontacts.domain.model.contact.ContactAccount
import com.alexstyl.contactstore.InternetAccount

fun ContactAccount.toInternetAccount(): InternetAccount =
    InternetAccount(name = username, type = accountProvider)

fun InternetAccount.toContactAccount(): ContactAccount =
    ContactAccount(username = name, accountProvider = type)
