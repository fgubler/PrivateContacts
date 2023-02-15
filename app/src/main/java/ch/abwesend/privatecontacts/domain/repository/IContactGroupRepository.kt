/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.repository

import ch.abwesend.privatecontacts.domain.model.contactgroup.IContactGroup
import ch.abwesend.privatecontacts.domain.model.result.ContactSaveResult

interface IContactGroupRepository {
    suspend fun createMissingContactGroups(contactGroups: List<IContactGroup>): ContactSaveResult
}
