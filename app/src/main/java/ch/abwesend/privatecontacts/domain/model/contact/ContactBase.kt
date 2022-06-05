/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.contact

interface IContactBase {
    val id: IContactId
    val type: ContactType
    val firstName: String
    val lastName: String
}

data class ContactBase(
    override val id: IContactId,
    override val type: ContactType,
    override val firstName: String,
    override val lastName: String,
) : IContactBase
