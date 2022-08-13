/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.testutil.databuilders

import ch.abwesend.privatecontacts.domain.model.contactgroup.ContactGroup
import ch.abwesend.privatecontacts.domain.model.contactgroup.ContactGroupId
import ch.abwesend.privatecontacts.infrastructure.room.contactgroup.ContactGroupEntity
import ch.abwesend.privatecontacts.infrastructure.room.contactgrouprelation.ContactGroupRelationEntity
import java.util.UUID

fun someContactGroupEntity(
    name: String = "SomeGroup",
    notes: String = "SomeGroup Description",
): ContactGroupEntity = ContactGroupEntity(
    name = name,
    notes = notes,
)

fun someContactGroupRelationEntity(
    groupName: String = "SomeGroup",
    contactId: UUID = UUID.randomUUID(),
): ContactGroupRelationEntity = ContactGroupRelationEntity(
    contactGroupName = groupName,
    contactId = contactId,
)

fun someContactGroup(
    name: String = "SomeGroup",
    notes: String = "SomeGroup Description",
): ContactGroup = ContactGroup(
    id = ContactGroupId(name = name),
    notes = notes,
)
