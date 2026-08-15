/*
 * Private Contacts
 * Copyright (c) 2025.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.service

import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.model.contact.IContactIdInternal
import ch.abwesend.privatecontacts.domain.model.contactgroup.IContactGroup
import ch.abwesend.privatecontacts.domain.repository.IAndroidContactLoadService
import ch.abwesend.privatecontacts.domain.repository.IContactGroupRepository
import ch.abwesend.privatecontacts.domain.util.injectAnywhere

class ContactGroupService {
    private val contactGroupRepository: IContactGroupRepository by injectAnywhere()
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
}
