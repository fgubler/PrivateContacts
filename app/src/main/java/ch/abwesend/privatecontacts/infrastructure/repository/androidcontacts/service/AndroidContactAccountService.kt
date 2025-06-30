/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.service

import ch.abwesend.privatecontacts.domain.model.contact.ContactAccount
import ch.abwesend.privatecontacts.domain.model.contact.IContact
import ch.abwesend.privatecontacts.domain.settings.Settings
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.mapping.toContactAccount
import ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.repository.AndroidContactLoadRepository
import com.alexstyl.contactstore.ContactGroup

class AndroidContactAccountService {
    private val contactLoadRepository: AndroidContactLoadRepository by injectAnywhere()

    /**
     * for an existing account, we don't know for certain which account it belongs to
     * => try guessing
     * TODO try guessing by the number of contacts (resolved by their groups)
     * TODO find a way which involves less guessing and more knowing
     * TODO test manually once groups can be edited
     */
    suspend fun getBestGuessForCorrespondingAccount(contact: IContact): ContactAccount =
        when (contact.saveInAccount) {
            is ContactAccount.OnlineAccount -> contact.saveInAccount
            is ContactAccount.LocalPhoneContacts -> contact.saveInAccount
            is ContactAccount.None -> {
                val accountFromGroups = contact.guessAccountFromContactGroups()
                accountFromGroups ?: Settings.nextOrDefault().defaultExternalContactAccount
            }
        }

    private suspend fun IContact.guessAccountFromContactGroups(): ContactAccount? {
        val contactGroupIds = contactGroups.mapNotNull { it.id.groupNo }
        val androidGroups = contactLoadRepository.loadContactGroupsByIds(contactGroupIds)
            .ifEmpty { contactLoadRepository.loadAllContactGroups() }
        return guessAccountFromContactGroups(androidGroups)
    }

    /** @return null if no groups were found, the most-used account otherwise */
    private fun guessAccountFromContactGroups(contactGroups: List<ContactGroup>): ContactAccount? =
        if (contactGroups.isNotEmpty()) {
            val groupsByAccount = contactGroups.groupBy { it.account } // here, null means "local phone contacts"
            val dominantAccount = groupsByAccount.maxByOrNull { (_, groups) -> groups.size }?.key
            dominantAccount.toContactAccount()
        } else null
}
