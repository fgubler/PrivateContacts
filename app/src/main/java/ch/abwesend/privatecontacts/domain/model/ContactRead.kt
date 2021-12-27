package ch.abwesend.privatecontacts.domain.model

import java.util.UUID

interface Contact {
    val id: UUID
    val firstName: String
    val lastName: String
    val phoneNumbers: List<PhoneNumber>
}

data class ContactRead(
    override val id: UUID,
    override val firstName: String,
    override val lastName: String,
    override val phoneNumbers: List<PhoneNumber>
): Contact
