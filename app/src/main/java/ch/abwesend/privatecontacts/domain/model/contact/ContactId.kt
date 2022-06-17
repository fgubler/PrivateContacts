/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.contact

import java.util.UUID

sealed interface ContactId

sealed interface IContactIdInternal : ContactId {
    val uuid: UUID
}

sealed interface IContactIdExternal : ContactId {
    val contactNo: Long
}

@JvmInline
value class ContactIdInternal(override val uuid: UUID) : IContactIdInternal {
    companion object {
        fun randomId(): ContactIdInternal = ContactIdInternal(UUID.randomUUID())
    }
}

@JvmInline
value class ContactIdAndroid(override val contactNo: Long) : IContactIdExternal
