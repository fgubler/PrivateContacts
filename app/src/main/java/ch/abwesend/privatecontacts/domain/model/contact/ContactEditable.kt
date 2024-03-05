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
    override var middleName: String
    override var namePrefix: String
    override var nameSuffix: String
    override var type: ContactType
    override var notes: String
    override var image: ContactImage
    override val isNew: Boolean
    override val contactDataSet: MutableList<ContactData>
    override val contactGroups: MutableList<ContactGroup>

    fun wrap(): ContactEditableWrapper
    fun deepCopy(isContactNew: Boolean = isNew, replaceId: Boolean = false): IContactEditable
}

data class ContactEditable(
    override val id: ContactId,
    override val importId: ContactImportId?,
    override var firstName: String,
    override var lastName: String,
    override var nickname: String,
    override var middleName: String,
    override var namePrefix: String,
    override var nameSuffix: String,
    override var type: ContactType,
    override var notes: String,
    override var image: ContactImage,
    override val contactDataSet: MutableList<ContactData>,
    override val contactGroups: MutableList<ContactGroup>,
    override var saveInAccount: ContactAccount,
    override val isNew: Boolean = false,
) : IContactEditable {
    override val displayName: String
        get() = getFullName()

    override fun wrap(): ContactEditableWrapper = ContactEditableWrapper(this)

    override fun deepCopy(isContactNew: Boolean, replaceId: Boolean): ContactEditable {
        val contactId = if (replaceId) ContactIdInternal.randomId() else id
        return copy(id = contactId, isNew = isContactNew, contactDataSet = contactDataSet.toMutableList())
    }

    companion object {
        fun createNew(importId: ContactImportId? = null): ContactEditable {
            val type = Settings.current.defaultContactType
            return ContactEditable(
                id = ContactIdInternal.randomId(),
                importId = importId,
                firstName = "",
                lastName = "",
                nickname = "",
                middleName = "",
                namePrefix = "",
                nameSuffix = "",
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
