package ch.abwesend.privatecontacts.domain.model.contact

import ch.abwesend.privatecontacts.domain.model.contactdata.ContactData
import ch.abwesend.privatecontacts.domain.model.contactdata.PhoneNumber
import java.util.UUID

interface Contact : ContactBase {
    val contactDataSet: List<ContactData>
    val phoneNumbers: List<PhoneNumber> // TODO remove?
    val isNew: Boolean
}

data class ContactFull(
    override val id: UUID,
    override val firstName: String,
    override val lastName: String,
    override val nickname: String,
    override val type: ContactType,
    override val notes: String,
    override val contactDataSet: List<ContactData>,
    override val isNew: Boolean = false,
) : Contact {
    override val phoneNumbers: List<PhoneNumber>
        get() = contactDataSet.filterIsInstance<PhoneNumber>()

    companion object
}
