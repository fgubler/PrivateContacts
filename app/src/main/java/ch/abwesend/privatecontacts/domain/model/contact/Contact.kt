package ch.abwesend.privatecontacts.domain.model.contact

import ch.abwesend.privatecontacts.domain.model.contactdata.ContactData
import java.util.UUID

interface IContact : IContactBase {
    val contactDataSet: List<ContactData>
    val isNew: Boolean
}

data class Contact(
    override val id: UUID,
    override val firstName: String,
    override val lastName: String,
    override val nickname: String,
    override val type: ContactType,
    override val notes: String,
    override val contactDataSet: List<ContactData>,
    override val isNew: Boolean = false,
) : IContact
