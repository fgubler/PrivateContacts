/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.contact

import ch.abwesend.privatecontacts.domain.model.contactdata.ContactData
import ch.abwesend.privatecontacts.domain.settings.Settings

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
    override val displayName: String
        get() = getFullName()

    override fun wrap(): ContactEditableWrapper = ContactEditableWrapper(this)
    fun deepCopy(): ContactEditable = copy(contactDataSet = contactDataSet.toMutableList())

    companion object {
        fun createNew(): ContactEditable =
            ContactEditable(
                id = ContactIdInternal.randomId(),
                firstName = "",
                lastName = "",
                nickname = "",
                type = Settings.current.defaultContactType,
                notes = "",
                contactDataSet = mutableListOf(),
                isNew = true,
            )
    }
}
