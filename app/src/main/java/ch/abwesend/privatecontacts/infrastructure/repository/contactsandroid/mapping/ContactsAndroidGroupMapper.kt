/*
 * Private Contacts
 * Copyright (c) 2026.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository.contactsandroid.mapping

import ch.abwesend.privatecontacts.domain.model.ModelStatus.UNCHANGED
import ch.abwesend.privatecontacts.domain.model.contactgroup.ContactGroup
import ch.abwesend.privatecontacts.domain.model.contactgroup.ContactGroupId
import contacts.core.entities.Group

fun Group.toContactGroup(): ContactGroup {
    val groupId = ContactGroupId(name = title, groupNo = id)
    return ContactGroup(id = groupId, notes = "", modelStatus = UNCHANGED)
}

fun List<Group>.toContactGroups(): List<ContactGroup> =
    map { it.toContactGroup() }
        .distinctBy { it.id.name }
