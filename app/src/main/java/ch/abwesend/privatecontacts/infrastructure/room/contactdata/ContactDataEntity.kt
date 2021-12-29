package ch.abwesend.privatecontacts.infrastructure.room.contactdata

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import ch.abwesend.privatecontacts.infrastructure.room.contact.ContactEntity

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
    @PrimaryKey(autoGenerate = true) val id: Int,
    val contactId: Int,
    val type: ContactDataType,
    val subType: ContactDataSubType,
    val customSubType: String?, // if the subtype is custom, the user can enter custom-subtype
    val sortOrder: Int,
    val value: String,
    val isMain: Boolean,
)
