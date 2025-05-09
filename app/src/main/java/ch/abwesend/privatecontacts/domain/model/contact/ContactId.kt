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

/**
 * Android contacts have a [contactNo] which is unique but can apparently change over time.
 * The [lookupKey] is not guaranteed to be present but should be longer-lived.
 */
sealed interface IContactIdExternal : ContactId {
    val contactNo: Long
    val lookupKey: String?
}

sealed interface IContactIdCombined : IContactIdInternal, IContactIdExternal

@JvmInline
value class ContactIdInternal(override val uuid: UUID) : IContactIdInternal {
    companion object {
        fun randomId(): ContactIdInternal = ContactIdInternal(UUID.randomUUID())
    }
}

data class ContactIdAndroid(override val contactNo: Long, override val lookupKey: String?) : IContactIdExternal

data class ContactIdCombined(
    override val uuid: UUID,
    override val contactNo: Long,
) : IContactIdCombined {
    // TODO store separately to allow a different lookupKey?
    override val lookupKey: String get() = uuid.toString()

    companion object {
        fun randomInternal(contactNo: Long): ContactIdCombined =
            ContactIdCombined(uuid = UUID.randomUUID(), contactNo = contactNo)
    }
}
