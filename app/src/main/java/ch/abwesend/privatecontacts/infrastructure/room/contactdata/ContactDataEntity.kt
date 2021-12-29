package ch.abwesend.privatecontacts.infrastructure.room.contactdata

import androidx.room.Embedded
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
        )
    ]
)
data class ContactDataEntity(
    @PrimaryKey val id: UUID,
    val contactId: UUID,
    val type: ContactDataType,
    @Embedded(prefix = "subType") val subType: ContactDataSubTypeEntity,
    val isMain: Boolean,
    val value: String,
    val sortOrder: Int? = null,
    val customSubType: String? = null, // if the subtype is custom, the user can enter custom-subtype
)
