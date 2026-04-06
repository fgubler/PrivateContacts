/*
 * Private Contacts
 * Copyright (c) 2026.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository.contactsandroid.service

import ch.abwesend.privatecontacts.domain.lib.flow.ResourceFlow
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.contact.ContactWithPhoneNumbers
import ch.abwesend.privatecontacts.domain.model.contact.IContact
import ch.abwesend.privatecontacts.domain.model.contact.IContactBase
import ch.abwesend.privatecontacts.domain.model.contact.IContactIdExternal
import ch.abwesend.privatecontacts.domain.model.contactgroup.ContactGroup
import ch.abwesend.privatecontacts.domain.model.search.ContactSearchConfig
import ch.abwesend.privatecontacts.domain.repository.IAndroidContactLoadService
import ch.abwesend.privatecontacts.domain.settings.Settings
import ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.service.AndroidContactLoadService

/**
 * Delegates to either the old (contactstore) or new (contacts-android) implementation
 * based on the [Settings.current.useNewContactsLibrary] feature flag.
 */
class DelegatingContactLoadService(
    private val oldService: AndroidContactLoadService,
    private val newService: ContactsAndroidLoadService,
) : IAndroidContactLoadService {

    private val delegate: IAndroidContactLoadService
        get() = if (Settings.current.useNewContactsLibrary) {
            logger.debug("Using contacts-android library for loading")
            newService
        } else {
            logger.debug("Using contact-store library for loading")
            oldService
        }

    override fun loadContactsAsFlow(searchConfig: ContactSearchConfig): ResourceFlow<List<IContactBase>> =
        delegate.loadContactsAsFlow(searchConfig)

    override suspend fun loadAllContactsFull(): List<IContact> =
        delegate.loadAllContactsFull()

    override suspend fun resolveContact(contactId: IContactIdExternal): IContact =
        delegate.resolveContact(contactId)

    override suspend fun resolveContacts(contactIds: Set<IContactIdExternal>): List<IContact> =
        delegate.resolveContacts(contactIds)

    override suspend fun getAllContactGroups(): List<ContactGroup> =
        delegate.getAllContactGroups()

    override suspend fun findContactsWithPhoneNumber(phoneNumber: String): List<ContactWithPhoneNumbers> =
        delegate.findContactsWithPhoneNumber(phoneNumber)
}
