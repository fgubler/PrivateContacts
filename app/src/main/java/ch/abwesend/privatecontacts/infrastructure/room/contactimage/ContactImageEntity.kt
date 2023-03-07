/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.room.contactimage

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import ch.abwesend.privatecontacts.infrastructure.room.contact.ContactEntity
import java.util.UUID

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = ContactEntity::class,
            parentColumns = ["id"],
            childColumns = ["contactId"],
            onDelete = ForeignKey.CASCADE
        ),
    ],
)
class ContactImageEntity(
    @PrimaryKey val contactId: UUID,
    val thumbnailUri: String?,
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    val fullImage: ByteArray?
)
