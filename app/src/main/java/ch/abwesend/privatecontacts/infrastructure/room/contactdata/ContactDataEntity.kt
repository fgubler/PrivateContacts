/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.room.contactdata

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataCategory
import ch.abwesend.privatecontacts.infrastructure.room.contact.ContactEntity
import java.util.UUID

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = ContactEntity::class,
            parentColumns = ["id"],
            childColumns = ["contactId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("contactId")
    ],
)
data class ContactDataEntity(
    @PrimaryKey val id: UUID,
    val contactId: UUID,
    val category: ContactDataCategory,
    @Embedded(prefix = "type") val type: ContactDataTypeEntity,
    val isMain: Boolean,
    val valueRaw: String,
    val valueFormatted: String,
    val sortOrder: Int,
)
