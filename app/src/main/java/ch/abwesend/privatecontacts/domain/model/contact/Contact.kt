package ch.abwesend.privatecontacts.domain.model.contact

import ch.abwesend.privatecontacts.domain.model.ContactBase
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactType
import ch.abwesend.privatecontacts.domain.model.contactdata.PhoneNumber
import java.util.UUID

interface Contact : ContactBase {
    val phoneNumbers: List<PhoneNumber>
    val notes: String
}

data class ContactFull(
    override val id: Int,
    override val uuid: UUID,
    override var firstName: String,
    override var lastName: String,
    override var nickname: String,
    override var notes: String,
    override var phoneNumbers: List<PhoneNumber>,
    override val type: ContactType,
) : Contact
