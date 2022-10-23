/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.testutil

import ch.abwesend.privatecontacts.domain.model.contact.ContactIdInternal
import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.model.contact.IContact
import ch.abwesend.privatecontacts.domain.model.contact.getFullName
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactData
import ch.abwesend.privatecontacts.domain.model.contactgroup.ContactGroup
import ch.abwesend.privatecontacts.domain.model.contactimage.ContactImage

data class TestContact(
    override val contactDataSet: List<ContactData>,
    override val contactGroups: List<ContactGroup>,
    override val firstName: String,
    override val lastName: String,
    override val nickname: String,
    override val notes: String,
    override val isNew: Boolean,
    override val id: ContactIdInternal,
    override val type: ContactType,
    override val image: ContactImage,
) : IContact {
    override val displayName: String
        get() = getFullName()
}
