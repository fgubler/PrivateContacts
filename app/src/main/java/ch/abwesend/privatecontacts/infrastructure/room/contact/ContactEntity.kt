/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.room.contact

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import ch.abwesend.privatecontacts.domain.model.contact.ContactIdCombined
import ch.abwesend.privatecontacts.domain.model.contact.ContactIdInternal
import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.model.contact.IContactIdInternal
import java.util.UUID

@Entity
data class ContactEntity(
    @PrimaryKey @ColumnInfo(name = "id") val rawId: UUID,
    val externalContactNo: Long?,
    val firstName: String,
    val lastName: String,
    val nickname: String,
    val type: ContactType,
    val notes: String,
    var fullTextSearch: String, // column optimized for full-text search
) {
    @Ignore val id: IContactIdInternal =
        externalContactNo?.let { ContactIdCombined(rawId, it) } ?: ContactIdInternal(rawId)
}
