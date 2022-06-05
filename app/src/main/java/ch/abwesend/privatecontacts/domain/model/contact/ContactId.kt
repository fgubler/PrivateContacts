/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.contact

import java.util.UUID

sealed interface IContactId

sealed interface IContactIdInternal : IContactId {
    val uuid: UUID
}

sealed interface IContactIdExternal : IContactId {
    val contactNo: Long
}

/** using value classes unfortunately breaks testing */
data class ContactIdInternal(override val uuid: UUID) : IContactIdInternal {
    companion object {
        fun randomId(): ContactIdInternal = ContactIdInternal(UUID.randomUUID())
    }
}
data class ContactIdAndroid(override val contactNo: Long) : IContactIdExternal
