/*
 * Private Contacts
 * Copyright (c) 2025.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.service

import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.model.contact.IContactIdInternal
import ch.abwesend.privatecontacts.domain.model.contactgroup.IContactGroup
import ch.abwesend.privatecontacts.domain.model.result.ContactDeleteResult
import ch.abwesend.privatecontacts.domain.model.result.ContactSaveResult
import ch.abwesend.privatecontacts.domain.repository.IAndroidContactLoadService
import ch.abwesend.privatecontacts.domain.repository.IContactGroupRepository
import ch.abwesend.privatecontacts.domain.repository.IContactRepository
import ch.abwesend.privatecontacts.domain.util.injectAnywhere

class ContactGroupService {
    private val contactGroupRepository: IContactGroupRepository by injectAnywhere()
    private val contactRepository: IContactRepository by injectAnywhere()
    private val androidContactService: IAndroidContactLoadService by injectAnywhere()

    /**
     * @param ignoreEmptyGroups if true, contact-groups without any contacts are filtered out.
     * Beware: only supported for contacts of type [ContactType.SECRET]; the flag is ignored for public groups.
     */
    suspend fun loadAllContactGroups(ignoreEmptyGroups: Boolean = false): List<IContactGroup> =
        (loadAllContactGroups(ContactType.PUBLIC, ignoreEmptyGroups) +
                loadAllContactGroups(ContactType.SECRET, ignoreEmptyGroups))
            .distinctBy { it.id.name }

    /**
     * @param ignoreEmptyGroups if true, contact-groups without any contacts are filtered out.
     * Beware: only supported for contacts of type [ContactType.SECRET]; the flag is ignored for public groups.
     */
    suspend fun loadAllContactGroups(contactType: ContactType, ignoreEmptyGroups: Boolean = false): List<IContactGroup> =
        when (contactType) {
            ContactType.SECRET -> contactGroupRepository.loadAllContactGroups(ignoreEmptyGroups)
            ContactType.PUBLIC -> androidContactService.getAllContactGroups()
        }

    /** beware: only supported for contacts of type [ContactType.SECRET] */
    suspend fun getContactIdsInGroups(groupNames: Collection<String>): Set<IContactIdInternal> =
        contactGroupRepository.getContactIdsInGroups(groupNames)

    /** beware: only supported for contacts of type [ContactType.SECRET] */
    suspend fun loadNumberOfContactsPerGroup(): Map<String, Int> =
        contactGroupRepository.loadNumberOfContactsPerGroup()

    /** beware: only supported for contacts of type [ContactType.SECRET] */
    suspend fun createContactGroup(contactGroup: IContactGroup): ContactSaveResult =
        contactGroupRepository.createMissingContactGroups(listOf(contactGroup))

    /** beware: only supported for contacts of type [ContactType.SECRET] */
    suspend fun updateContactGroup(oldGroup: IContactGroup, newGroup: IContactGroup): ContactSaveResult {
        val result = contactGroupRepository.updateContactGroup(oldGroup, newGroup)
        val nameChanged = oldGroup.id.name != newGroup.id.name

        if (result is ContactSaveResult.Success && nameChanged) {
            val contactIds = contactGroupRepository.getContactIdsInGroups(listOf(newGroup.id.name))
            recomputeFullTextSearch(contactIds)
        }

        return result
    }

    /** beware: only supported for contacts of type [ContactType.SECRET] */
    suspend fun deleteContactGroup(contactGroup: IContactGroup): ContactDeleteResult {
        // must be loaded before the deletion: the relations are deleted with the group
        val contactIds = contactGroupRepository.getContactIdsInGroups(listOf(contactGroup.id.name))
        val result = contactGroupRepository.deleteContactGroup(contactGroup)

        if (result is ContactDeleteResult.Success) {
            recomputeFullTextSearch(contactIds)
        }

        return result
    }

    private suspend fun recomputeFullTextSearch(contactIds: Set<IContactIdInternal>) {
        val result = contactRepository.recomputeFullTextSearch(contactIds)
        if (result !is ContactSaveResult.Success) {
            logger.warning("Failed to re-compute the full-text-search of some of ${contactIds.size} contacts")
        }
    }
}
