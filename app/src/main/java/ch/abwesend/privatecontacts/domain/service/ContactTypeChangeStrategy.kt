/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.service

import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.model.contact.IContact
import ch.abwesend.privatecontacts.domain.repository.IAndroidContactSaveService
import ch.abwesend.privatecontacts.domain.repository.IContactGroupRepository
import ch.abwesend.privatecontacts.domain.util.injectAnywhere

sealed interface ContactTypeChangeStrategy {
    suspend fun createContactGroups(contacts: List<IContact>)
    val correspondingContactType: ContactType
    val deleteOldContactAfterCreatingNew: Boolean

    companion object {
        fun fromContactType(targetType: ContactType): ContactTypeChangeStrategy =
            when (targetType) {
                ContactType.SECRET -> ChangeContactToSecretStrategy
                ContactType.PUBLIC -> ChangeContactToPublicStrategy
            }
    }
}

object ChangeContactToSecretStrategy : ContactTypeChangeStrategy {
    private val contactGroupRepository: IContactGroupRepository by injectAnywhere()

    override val correspondingContactType: ContactType = ContactType.SECRET
    override val deleteOldContactAfterCreatingNew: Boolean = true

    override suspend fun createContactGroups(contacts: List<IContact>) {
        val contactGroups = contacts.flatMap { it.contactGroups }
        contactGroupRepository.createMissingContactGroups(contactGroups)
        // if it fails, it will later be created individually: is slower but still works
    }
}

object ChangeContactToPublicStrategy : ContactTypeChangeStrategy {
    private val contactSaveService: IAndroidContactSaveService by injectAnywhere()

    override val correspondingContactType: ContactType = ContactType.PUBLIC
    override val deleteOldContactAfterCreatingNew: Boolean = true

    override suspend fun createContactGroups(contacts: List<IContact>) {
        val contactGroupsByAccount = contacts
            .groupBy { it.saveInAccount }
            .mapValues { (_, correspondingContacts) ->
                correspondingContacts.flatMap { it.contactGroups }
            }

        contactGroupsByAccount.forEach { (account, correspondingContacts) ->
            contactSaveService.createMissingContactGroups(account, correspondingContacts)
            // if it fails, it will later be created individually: is slower but still works
        }
    }
}
