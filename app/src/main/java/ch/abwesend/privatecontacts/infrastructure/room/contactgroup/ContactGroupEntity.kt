/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.room.contactgroup

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import ch.abwesend.privatecontacts.domain.model.contactgroup.ContactGroupId
import ch.abwesend.privatecontacts.domain.model.contactgroup.IContactGroupId

@Entity
data class ContactGroupEntity(
    @PrimaryKey val name: String,
) {
    @Ignore
    val id: IContactGroupId = ContactGroupId(name)
}
