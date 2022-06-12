/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.repository

import ch.abwesend.privatecontacts.domain.lib.flow.ResourceFlow
import ch.abwesend.privatecontacts.domain.model.contact.IContactBase

interface IAndroidContactRepository {
    suspend fun loadContactsAsFlow(): ResourceFlow<List<IContactBase>>
}
