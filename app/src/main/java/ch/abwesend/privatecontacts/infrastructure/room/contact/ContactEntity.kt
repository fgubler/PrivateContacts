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
import ch.abwesend.privatecontacts.domain.model.contact.ContactId
import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.model.contact.IContactBase
import java.util.UUID

@Entity
data class ContactEntity(
    @PrimaryKey @ColumnInfo(name = "id") val rawId: UUID,
    override val firstName: String,
    override val lastName: String,
    override val nickname: String,
    override val type: ContactType,
    override val notes: String,
    var fullTextSearch: String, // column optimized for full-text search
) : IContactBase {
    @Ignore override val id: ContactId = ContactId(rawId)
}
