/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.repository

import ch.abwesend.privatecontacts.domain.model.contactgroup.IContactGroup

interface IContactGroupRepository {
    suspend fun createMissingContactGroups(contactGroups: List<IContactGroup>)
}
