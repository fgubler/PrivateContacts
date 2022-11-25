/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.room.contactgroup

import ch.abwesend.privatecontacts.domain.model.ModelStatus.UNCHANGED
import ch.abwesend.privatecontacts.domain.model.contactgroup.ContactGroup
import ch.abwesend.privatecontacts.domain.model.contactgroup.ContactGroupId
import ch.abwesend.privatecontacts.domain.model.contactgroup.IContactGroup

fun IContactGroup.toEntity(): ContactGroupEntity = ContactGroupEntity(name = id.name, notes = notes)

fun ContactGroupEntity.toContactGroup(): ContactGroup =
    ContactGroup(id = ContactGroupId(name = name), notes = notes, modelStatus = UNCHANGED)
