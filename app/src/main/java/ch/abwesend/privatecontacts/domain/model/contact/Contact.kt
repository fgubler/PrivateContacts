/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.contact

import ch.abwesend.privatecontacts.domain.model.contactdata.ContactData

interface IContactBase {
    val id: IContactId
    val type: ContactType
    val displayName: String
}

interface IContact : IContactBase {
    val firstName: String
    val lastName: String
    val nickname: String
    val notes: String
    val contactDataSet: List<ContactData>
    val isNew: Boolean
}

data class ContactBase(
    override val id: IContactId,
    override val type: ContactType,
    override val displayName: String,
) : IContactBase
