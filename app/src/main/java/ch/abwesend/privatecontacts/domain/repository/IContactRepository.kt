/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.repository

import ch.abwesend.privatecontacts.domain.model.contact.IContact
import ch.abwesend.privatecontacts.domain.model.contact.IContactBase
import ch.abwesend.privatecontacts.domain.model.result.ContactDeleteResult
import ch.abwesend.privatecontacts.domain.model.result.ContactSaveResult
import ch.abwesend.privatecontacts.domain.model.search.ContactSearchConfig

interface IContactRepository {
    suspend fun loadContacts(): List<IContactBase>

    suspend fun getContactsPaged(
        searchConfig: ContactSearchConfig,
        loadSize: Int,
        offsetInRows: Int
    ): List<IContactBase>

    suspend fun resolveContact(contact: IContactBase): IContact
    suspend fun createContact(contact: IContact): ContactSaveResult
    suspend fun updateContact(contact: IContact): ContactSaveResult
    suspend fun deleteContact(contact: IContactBase): ContactDeleteResult
}
