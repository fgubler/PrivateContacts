package ch.abwesend.privatecontacts.domain.model.contact

import ch.abwesend.privatecontacts.domain.model.contactdata.PhoneNumber
import java.util.UUID

interface Contact : ContactBase {
    val phoneNumbers: List<PhoneNumber>
    val isNew: Boolean
}

data class ContactFull(
    override val id: UUID,
    override val firstName: String,
    override val lastName: String,
    override val nickname: String,
    override val type: ContactType,
    override val notes: String,
    override val phoneNumbers: List<PhoneNumber>,
    override val isNew: Boolean = false,
) : Contact {
    companion object
}
