/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.room.contactgroup

import ch.abwesend.privatecontacts.domain.model.contactgroup.ContactGroup
import ch.abwesend.privatecontacts.domain.model.contactgroup.ContactGroupId

fun ContactGroup.toEntity(): ContactGroupEntity = ContactGroupEntity(name = id.name)

fun ContactGroupEntity.toContactGroup(): ContactGroup =
    ContactGroup(id = ContactGroupId(name = name))
