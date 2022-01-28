package ch.abwesend.privatecontacts.domain.model.contact

import ch.abwesend.privatecontacts.domain.model.contactdata.ContactData

interface IContact : IContactBase {
    val contactDataSet: List<ContactData>
    val isNew: Boolean
}

data class Contact(
    override val id: ContactId,
    override val firstName: String,
    override val lastName: String,
    override val nickname: String,
    override val type: ContactType,
    override val notes: String,
    override val contactDataSet: List<ContactData>,
    override val isNew: Boolean = false,
) : IContact
