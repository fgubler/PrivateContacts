/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.repository

import ch.abwesend.privatecontacts.domain.model.contact.IContact
import ch.abwesend.privatecontacts.domain.model.contact.IContactIdExternal
import ch.abwesend.privatecontacts.domain.model.contact.IContactIdInternal
import ch.abwesend.privatecontacts.domain.model.result.ContactSaveResult
import ch.abwesend.privatecontacts.domain.model.result.batch.ContactBatchChangeResult

interface IAndroidContactSaveRepository {
    suspend fun deleteContacts(contactIds: List<IContactIdExternal>): ContactBatchChangeResult
    suspend fun updateContact(contactId: IContactIdExternal, contact: IContact): ContactSaveResult
    suspend fun createContact(contactId: IContactIdInternal, contact: IContact): ContactSaveResult
}
