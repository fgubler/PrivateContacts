/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.contact

import ch.abwesend.privatecontacts.domain.model.contactdata.ContactData
import ch.abwesend.privatecontacts.domain.model.contactgroup.ContactGroup
import ch.abwesend.privatecontacts.domain.model.contactimage.ContactImage

interface IContactBase {
    val id: ContactId
    val type: ContactType
    val displayName: String
}

interface IContact : IContactBase {
    val firstName: String
    val lastName: String
    val nickname: String
    val notes: String
    val contactDataSet: List<ContactData>
    val contactGroups: List<ContactGroup>
    val image: ContactImage
    val isNew: Boolean

    /** only relevant for new, external contacts */
    val saveInAccount: ContactAccount?
}

data class ContactBase(
    override val id: ContactId,
    override val type: ContactType,
    override val displayName: String,
) : IContactBase
