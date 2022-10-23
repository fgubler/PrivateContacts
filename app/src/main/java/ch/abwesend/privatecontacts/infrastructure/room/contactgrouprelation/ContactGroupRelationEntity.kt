/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.room.contactgrouprelation

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import ch.abwesend.privatecontacts.infrastructure.room.contact.ContactEntity
import ch.abwesend.privatecontacts.infrastructure.room.contactgroup.ContactGroupEntity
import java.util.UUID

@Entity(
    primaryKeys = ["contactGroupName", "contactId"],
    foreignKeys = [
        ForeignKey(
            entity = ContactEntity::class,
            parentColumns = ["id"],
            childColumns = ["contactId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ContactGroupEntity::class,
            parentColumns = ["name"],
            childColumns = ["contactGroupName"],
            onDelete = ForeignKey.CASCADE
        ),
    ],
    indices = [
        Index("contactId"),
        Index("contactId", "contactGroupName"),
    ],
)
data class ContactGroupRelationEntity(
    val contactGroupName: String,
    val contactId: UUID,
)
