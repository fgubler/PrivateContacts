/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.repository

import ch.abwesend.privatecontacts.domain.model.contact.ContactWithPhoneNumbers
import ch.abwesend.privatecontacts.domain.model.contact.IContact
import ch.abwesend.privatecontacts.domain.model.contact.IContactBase
import ch.abwesend.privatecontacts.domain.model.contact.IContactIdInternal
import ch.abwesend.privatecontacts.domain.model.result.ContactDeleteResult
import ch.abwesend.privatecontacts.domain.model.result.ContactSaveResult
import ch.abwesend.privatecontacts.domain.model.search.ContactSearchConfig

/**
 * Methods demanding both a contactId and a contact are declared this way to
 * restrict the type of the ID. The contact.id property will be ignored.
 */
interface IContactRepository {
    suspend fun loadContacts(): List<IContactBase>

    suspend fun getContactsPaged(
        searchConfig: ContactSearchConfig,
        loadSize: Int,
        offsetInRows: Int
    ): List<IContactBase>

    suspend fun findContactsWithNumberEndingOn(endOfPhoneNumber: String): List<ContactWithPhoneNumbers>

    suspend fun resolveContact(contactId: IContactIdInternal): IContact
    suspend fun createContact(contactId: IContactIdInternal, contact: IContact): ContactSaveResult
    suspend fun updateContact(contactId: IContactIdInternal, contact: IContact): ContactSaveResult
    suspend fun deleteContacts(contactIds: Collection<IContactIdInternal>): ContactDeleteResult
}
