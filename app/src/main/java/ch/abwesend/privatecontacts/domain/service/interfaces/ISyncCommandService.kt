/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.service.interfaces

import ch.abwesend.privatecontacts.domain.model.contact.IContact
import ch.abwesend.privatecontacts.domain.model.contact.IContactIdInternal
import ch.abwesend.privatecontacts.domain.model.sync.SyncCommandOperation

/**
 * Turns local secret-contact changes into pending sync commands (outbox rows). All operations are
 * no-ops when sync is disabled. Never called for public contacts.
 */
interface ISyncCommandService {
    suspend fun enqueueUpsert(
        contactId: IContactIdInternal,
        contact: IContact,
        operation: SyncCommandOperation,
    )

    suspend fun enqueueDeletes(contactIds: Collection<IContactIdInternal>)
}
