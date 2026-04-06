/*
 * Private Contacts
 * Copyright (c) 2026.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository.contactsandroid.service

import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.contact.ContactAccount
import ch.abwesend.privatecontacts.domain.model.contact.IContact
import ch.abwesend.privatecontacts.domain.model.contact.IContactIdExternal
import ch.abwesend.privatecontacts.domain.model.contactgroup.IContactGroup
import ch.abwesend.privatecontacts.domain.model.result.ContactSaveResult
import ch.abwesend.privatecontacts.domain.model.result.batch.ContactIdBatchChangeResult
import ch.abwesend.privatecontacts.domain.repository.IAndroidContactSaveService
import ch.abwesend.privatecontacts.domain.settings.Settings

/**
 * Delegates to either the old (contactstore) or new (contacts-android) implementation
 * based on the [Settings.current.useNewContactsLibrary] feature flag.
 */
class DelegatingContactSaveService(
    private val oldService: IAndroidContactSaveService,
    private val newService: IAndroidContactSaveService,
) : IAndroidContactSaveService {

    private val delegate: IAndroidContactSaveService
        get() = if (Settings.current.useNewContactsLibrary) {
            logger.debug("Using contacts-android library for saving")
            newService
        } else {
            logger.debug("Using contact-store library for saving")
            oldService
        }

    override suspend fun deleteContacts(contactIds: Collection<IContactIdExternal>): ContactIdBatchChangeResult =
        delegate.deleteContacts(contactIds)

    override suspend fun updateContact(contactId: IContactIdExternal, contact: IContact): ContactSaveResult =
        delegate.updateContact(contactId, contact)

    override suspend fun createContact(contact: IContact): ContactSaveResult =
        delegate.createContact(contact)

    override suspend fun createMissingContactGroups(account: ContactAccount, groups: List<IContactGroup>): ContactSaveResult =
        delegate.createMissingContactGroups(account, groups)
}
