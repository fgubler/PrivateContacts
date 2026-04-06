/*
 * Private Contacts
 * Copyright (c) 2026.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository.contactsandroid.service

import ch.abwesend.privatecontacts.domain.lib.flow.ResourceFlow
import ch.abwesend.privatecontacts.domain.model.contact.ContactWithPhoneNumbers
import ch.abwesend.privatecontacts.domain.model.contact.IContact
import ch.abwesend.privatecontacts.domain.model.contact.IContactBase
import ch.abwesend.privatecontacts.domain.model.contact.IContactIdExternal
import ch.abwesend.privatecontacts.domain.model.contactgroup.ContactGroup
import ch.abwesend.privatecontacts.domain.model.search.ContactSearchConfig
import ch.abwesend.privatecontacts.domain.repository.IAndroidContactLoadService

/**
 * New implementation of [IAndroidContactLoadService] using the contacts-android library.
 * Stub for now — will be implemented in Phase 1 (reading contacts) and Phase 2 (reading groups).
 */
class ContactsAndroidLoadService : IAndroidContactLoadService {
    override fun loadContactsAsFlow(searchConfig: ContactSearchConfig): ResourceFlow<List<IContactBase>> {
        TODO("Phase 1: reading contacts via contacts-android")
    }

    override suspend fun loadAllContactsFull(): List<IContact> {
        TODO("Phase 1: reading contacts via contacts-android")
    }

    override suspend fun resolveContact(contactId: IContactIdExternal): IContact {
        TODO("Phase 1: reading contacts via contacts-android")
    }

    override suspend fun resolveContacts(contactIds: Set<IContactIdExternal>): List<IContact> {
        TODO("Phase 1: reading contacts via contacts-android")
    }

    override suspend fun getAllContactGroups(): List<ContactGroup> {
        TODO("Phase 2: reading groups via contacts-android")
    }

    override suspend fun findContactsWithPhoneNumber(phoneNumber: String): List<ContactWithPhoneNumbers> {
        TODO("Phase 1: reading contacts via contacts-android")
    }
}
