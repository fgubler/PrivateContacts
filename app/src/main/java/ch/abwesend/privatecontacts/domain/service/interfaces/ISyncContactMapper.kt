/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.service.interfaces

import ch.abwesend.privatecontacts.domain.model.contact.IContact
import ch.abwesend.privatecontacts.domain.model.contact.IContactEditable
import ch.abwesend.privatecontacts.domain.model.contact.IContactIdInternal
import ch.abwesend.privatecontacts.domain.model.sync.SyncContact

/**
 * Maps between the internal [IContact] and the serializable [SyncContact] transport-DTO, and
 * (de)serializes contacts to/from the plaintext payload stored in the outbox. Behind an interface
 * so the domain layer can use it without depending on the infrastructure implementation.
 */
interface ISyncContactMapper {
    fun toSyncContact(contact: IContact, syncId: String): SyncContact
    fun toContact(syncContact: SyncContact, contactId: IContactIdInternal): IContactEditable
    fun serializeContact(contact: SyncContact): String
    fun deserializeContact(payload: String): SyncContact
}
