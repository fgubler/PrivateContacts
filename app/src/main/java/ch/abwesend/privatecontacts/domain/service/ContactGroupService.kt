/*
 * Private Contacts
 * Copyright (c) 2025.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.service

import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.model.contactgroup.IContactGroup
import ch.abwesend.privatecontacts.domain.repository.IAndroidContactLoadService
import ch.abwesend.privatecontacts.domain.repository.IContactGroupRepository
import ch.abwesend.privatecontacts.domain.util.injectAnywhere

class ContactGroupService {
    private val contactGroupRepository: IContactGroupRepository by injectAnywhere()
    private val androidContactService: IAndroidContactLoadService by injectAnywhere()

    suspend fun loadAllContactGroups(): List<IContactGroup> =
        (loadAllContactGroups(ContactType.PUBLIC) + loadAllContactGroups(ContactType.SECRET))
            .distinctBy { it.id.name }

    suspend fun loadAllContactGroups(contactType: ContactType): List<IContactGroup> =
        when (contactType) {
            ContactType.SECRET -> contactGroupRepository.loadAllContactGroups()
            ContactType.PUBLIC -> androidContactService.getAllContactGroups()
        }
}
