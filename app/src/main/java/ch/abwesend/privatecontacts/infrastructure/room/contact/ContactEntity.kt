package ch.abwesend.privatecontacts.infrastructure.room.contact

import androidx.room.Entity
import androidx.room.PrimaryKey
import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.model.contact.IContactBase
import java.util.UUID

@Entity
data class ContactEntity(
    @PrimaryKey override val id: UUID,
    override val firstName: String,
    override val lastName: String,
    override val nickname: String,
    override val type: ContactType,
    override val notes: String,
    var fullTextSearch: String, // column optimized for full-text search
) : IContactBase
