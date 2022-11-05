/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.repository

import ch.abwesend.privatecontacts.domain.model.contact.IContactIdExternal
import ch.abwesend.privatecontacts.domain.model.result.batch.ContactBatchChangeResult

interface IAndroidContactSaveRepository {
    suspend fun deleteContacts(contactIds: List<IContactIdExternal>): ContactBatchChangeResult
}
