/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.repository

import ch.abwesend.privatecontacts.domain.model.contact.IContactIdInternal
import ch.abwesend.privatecontacts.domain.model.contactgroup.IContactGroup
import ch.abwesend.privatecontacts.domain.model.result.ContactDeleteResult
import ch.abwesend.privatecontacts.domain.model.result.ContactSaveResult

interface IContactGroupRepository {
    suspend fun createMissingContactGroups(contactGroups: List<IContactGroup>): ContactSaveResult
    suspend fun loadAllContactGroups(ignoreEmptyGroups: Boolean = false): List<IContactGroup>
    suspend fun getContactIdsInGroups(groupNames: Collection<String>): Set<IContactIdInternal>

    /** @return the number of contacts per group-name, including groups without contacts */
    suspend fun loadNumberOfContactsPerGroup(): Map<String, Int>

    /** Beware: does not update the full-text-search of the contacts. */
    suspend fun updateContactGroup(oldGroup: IContactGroup, newGroup: IContactGroup): ContactSaveResult

    /** Beware: does not update the full-text-search of the contacts. */
    suspend fun deleteContactGroup(contactGroup: IContactGroup): ContactDeleteResult
}
