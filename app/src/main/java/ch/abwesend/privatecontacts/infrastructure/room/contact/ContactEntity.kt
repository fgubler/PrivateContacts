package ch.abwesend.privatecontacts.infrastructure.room.contact

import androidx.room.Entity
import androidx.room.PrimaryKey
import ch.abwesend.privatecontacts.domain.model.ContactBase

@Entity
data class ContactEntity(
    @PrimaryKey(autoGenerate = true) override val id: Int,
    override val firstName: String,
    override val lastName: String,
    override val nickname: String,
) : ContactBase

fun ContactBase.toEntity() = ContactEntity(
    id = id,
    firstName = firstName,
    lastName = lastName,
    nickname = nickname,
)
