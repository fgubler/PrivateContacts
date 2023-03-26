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
    override var isNew: Boolean
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
    override var saveInAccount: ContactAccount,
    override var isNew: Boolean = false,
) : IContactEditable {
    override val displayName: String
        get() = getFullName()

    override fun wrap(): ContactEditableWrapper = ContactEditableWrapper(this)

    override fun deepCopy(isContactNew: Boolean): ContactEditable =
        copy(isNew = isContactNew, contactDataSet = contactDataSet.toMutableList())

    companion object {
        fun createNew(): ContactEditable {
            val id = ContactIdInternal.randomId()
            val type = Settings.current.defaultContactType
            return ContactEditable(
                id = id,
                firstName = "",
                lastName = "",
                nickname = "",
                type = type,
                notes = "",
                image = ContactImage.empty,
                contactDataSet = mutableListOf(),
                contactGroups = mutableListOf(),
                saveInAccount = ContactAccount.currentDefaultForContactType(type),
                isNew = true,
            )
        }
    }
}
