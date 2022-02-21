/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.contact

import java.util.UUID

interface IContactBase {
    val id: ContactId
    val firstName: String
    val lastName: String
    val nickname: String
    val type: ContactType
    val notes: String
}

data class ContactBase(
    override val id: ContactId,
    override val firstName: String,
    override val lastName: String,
    override val nickname: String,
    override val type: ContactType,
    override val notes: String,
) : IContactBase

@JvmInline
value class ContactId(val uuid: UUID) {
    companion object {
        fun randomId(): ContactId = ContactId(UUID.randomUUID())
    }
}
