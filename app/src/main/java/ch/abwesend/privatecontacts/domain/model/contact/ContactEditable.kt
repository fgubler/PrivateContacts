/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.contact

import ch.abwesend.privatecontacts.domain.model.contactdata.ContactData
import ch.abwesend.privatecontacts.domain.model.contactgroup.ContactGroup
import ch.abwesend.privatecontacts.domain.model.contactimage.ContactImage
import ch.abwesend.privatecontacts.domain.settings.Settings

interface IContactEditable : IContact, IContactBaseWithAccountInformation {
    override var firstName: String
    override var lastName: String
    override var nickname: String
    override var type: ContactType
    override var notes: String
    override var image: ContactImage
    override val contactDataSet: MutableList<ContactData>
    override val contactGroups: MutableList<ContactGroup>

    fun wrap(): ContactEditableWrapper
    fun deepCopy(isContactNew: Boolean = isNew): IContactEditable
}

data class ContactEditable(
    override val id: ContactId,
    override var firstName: String,
    override var lastName: String,
    override var nickname: String,
    override var type: ContactType,
    override var notes: String,
    override var image: ContactImage,
    override val contactDataSet: MutableList<ContactData>,
    override val contactGroups: MutableList<ContactGroup>,
    override val isNew: Boolean = false,
    override var saveInAccount: ContactAccount? = null,
) : IContactEditable {
    override val displayName: String
        get() = getFullName()

    override fun wrap(): ContactEditableWrapper = ContactEditableWrapper(this)

    override fun deepCopy(isContactNew: Boolean): ContactEditable =
        copy(isNew = isContactNew, contactDataSet = contactDataSet.toMutableList())

    companion object {
        fun createNew(): ContactEditable {
            val id = ContactIdInternal.randomId()
            return ContactEditable(
                id = id,
                firstName = "",
                lastName = "",
                nickname = "",
                type = Settings.current.defaultContactType,
                notes = "",
                image = ContactImage.empty,
                contactDataSet = mutableListOf(),
                contactGroups = mutableListOf(),
                isNew = true,
            )
        }
    }
}
