/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.repository

import ch.abwesend.privatecontacts.domain.model.contact.ContactAccount
import ch.abwesend.privatecontacts.domain.model.contact.IContact
import ch.abwesend.privatecontacts.domain.model.contact.IContactIdExternal
import ch.abwesend.privatecontacts.domain.model.contactgroup.ContactGroup
import ch.abwesend.privatecontacts.domain.model.result.ContactSaveResult
import ch.abwesend.privatecontacts.domain.model.result.batch.ContactBatchChangeResult

interface IAndroidContactSaveService {
    suspend fun deleteContacts(contactIds: Collection<IContactIdExternal>): ContactBatchChangeResult
    suspend fun updateContact(contactId: IContactIdExternal, contact: IContact): ContactSaveResult
    suspend fun createContact(contact: IContact): ContactSaveResult
    suspend fun createMissingContactGroups(account: ContactAccount, groups: List<ContactGroup>): ContactSaveResult
}
