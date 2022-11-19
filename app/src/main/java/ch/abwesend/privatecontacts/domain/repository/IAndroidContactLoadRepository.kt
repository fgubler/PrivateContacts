/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.repository

import ch.abwesend.privatecontacts.domain.lib.flow.ResourceFlow
import ch.abwesend.privatecontacts.domain.model.contact.IContact
import ch.abwesend.privatecontacts.domain.model.contact.IContactBase
import ch.abwesend.privatecontacts.domain.model.contact.IContactIdExternal
import ch.abwesend.privatecontacts.domain.model.search.ContactSearchConfig

interface IAndroidContactLoadRepository {
    fun loadContactsAsFlow(searchConfig: ContactSearchConfig): ResourceFlow<List<IContactBase>>
    suspend fun resolveContact(contactId: IContactIdExternal): IContact
}
