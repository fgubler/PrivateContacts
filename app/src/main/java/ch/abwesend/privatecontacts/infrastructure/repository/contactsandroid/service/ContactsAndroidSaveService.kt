/*
 * Private Contacts
 * Copyright (c) 2026.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository.contactsandroid.service

import ch.abwesend.privatecontacts.domain.model.contact.ContactAccount
import ch.abwesend.privatecontacts.domain.model.contact.IContact
import ch.abwesend.privatecontacts.domain.model.contact.IContactIdExternal
import ch.abwesend.privatecontacts.domain.model.contactgroup.IContactGroup
import ch.abwesend.privatecontacts.domain.model.result.ContactSaveResult
import ch.abwesend.privatecontacts.domain.model.result.batch.ContactIdBatchChangeResult
import ch.abwesend.privatecontacts.domain.repository.IAndroidContactSaveService

/**
 * New implementation of [IAndroidContactSaveService] using the contacts-android library.
 * Stub for now — will be implemented in Phase 3 (creating) and Phase 5 (editing).
 */
class ContactsAndroidSaveService : IAndroidContactSaveService {
    override suspend fun deleteContacts(contactIds: Collection<IContactIdExternal>): ContactIdBatchChangeResult {
        TODO("Phase 5: editing/deleting contacts via contacts-android")
    }

    override suspend fun updateContact(contactId: IContactIdExternal, contact: IContact): ContactSaveResult {
        TODO("Phase 5: editing contacts via contacts-android")
    }

    override suspend fun createContact(contact: IContact): ContactSaveResult {
        TODO("Phase 3: creating contacts via contacts-android")
    }

    override suspend fun createMissingContactGroups(account: ContactAccount, groups: List<IContactGroup>): ContactSaveResult {
        TODO("Phase 4: creating groups via contacts-android")
    }
}
