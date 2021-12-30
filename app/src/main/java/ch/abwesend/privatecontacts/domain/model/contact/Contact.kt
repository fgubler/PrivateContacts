package ch.abwesend.privatecontacts.domain.model.contact

import ch.abwesend.privatecontacts.domain.model.contactdata.PhoneNumber
import java.util.UUID

interface Contact : ContactBase {
    val phoneNumbers: List<PhoneNumber>
}

data class ContactEditable(
    override var id: UUID,
    override var firstName: String,
    override var lastName: String,
    override var nickname: String,
    override var type: ContactType,
    override var notes: String,
    override val phoneNumbers: MutableList<PhoneNumber>,
    val isNew: Boolean = false,
) : Contact {
    companion object
}
