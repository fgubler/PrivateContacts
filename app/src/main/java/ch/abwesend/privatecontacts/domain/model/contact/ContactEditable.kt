package ch.abwesend.privatecontacts.domain.model.contact

import ch.abwesend.privatecontacts.domain.Settings
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactData

interface IContactEditable : IContact {
    override var firstName: String
    override var lastName: String
    override var nickname: String
    override var type: ContactType
    override var notes: String
    override val contactDataSet: MutableList<ContactData>

    fun wrap(): ContactEditableWrapper
}

data class ContactEditable(
    override val id: ContactId,
    override var firstName: String,
    override var lastName: String,
    override var nickname: String,
    override var type: ContactType,
    override var notes: String,
    override val contactDataSet: MutableList<ContactData>,
    override val isNew: Boolean = false,
) : IContactEditable {
    override fun wrap(): ContactEditableWrapper = ContactEditableWrapper(this)
    fun deepCopy(): ContactEditable = copy(contactDataSet = contactDataSet.toMutableList())

    companion object {
        fun createNew(): ContactEditable =
            ContactEditable(
                id = ContactId.randomId(),
                firstName = "",
                lastName = "",
                nickname = "",
                type = Settings.defaultContactType,
                notes = "",
                contactDataSet = mutableListOf(),
                isNew = true,
            )
    }
}
