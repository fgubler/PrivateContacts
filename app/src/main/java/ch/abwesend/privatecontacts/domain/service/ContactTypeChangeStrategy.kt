/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.service

import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.model.contact.IContact
import ch.abwesend.privatecontacts.domain.model.contact.IContactEditable
import ch.abwesend.privatecontacts.domain.repository.IContactGroupRepository
import ch.abwesend.privatecontacts.domain.util.injectAnywhere

sealed interface ContactTypeChangeStrategy {
    suspend fun createContactGroups(contacts: List<IContact>)
    fun changeContactDataIds(contact: IContactEditable)
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
        try {
            val contactGroups = contacts.flatMap { it.contactGroups }
            contactGroupRepository.createMissingContactGroups(contactGroups) // bulk-operation
        } catch (e: Exception) {
            logger.error("Failed to create contact groups as batch-operation", e)
            // will be created individually, instead
        }
    }

    override fun changeContactDataIds(contact: IContactEditable) {
        contact.contactDataSet.replaceAll { contactData -> contactData.changeToInternalId() }
    }
}

object ChangeContactToPublicStrategy : ContactTypeChangeStrategy {
    override val correspondingContactType: ContactType = ContactType.PUBLIC
    override val deleteOldContactAfterCreatingNew: Boolean = true

    override suspend fun createContactGroups(contacts: List<IContact>) {
        // TODO implement to store contact-groups as well
    }

    override fun changeContactDataIds(contact: IContactEditable) {
        contact.contactDataSet.replaceAll { contactData -> contactData.changeToExternalId() }
    }
}
