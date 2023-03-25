/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.contactdata

import java.util.UUID

sealed interface ContactDataId

sealed interface IContactDataIdInternal : ContactDataId {
    val uuid: UUID
}

sealed interface IContactDataIdExternal : ContactDataId {
    val contactDataNo: Long?
}

@JvmInline
value class ContactDataIdInternal(override val uuid: UUID) : IContactDataIdInternal {
    companion object {
        fun randomId(): ContactDataIdInternal = ContactDataIdInternal(UUID.randomUUID())
    }
}

@JvmInline
value class ContactDataIdAndroid(override val contactDataNo: Long) : IContactDataIdExternal

/**
 * The contactDataNo on android contacts is nullable:
 * we still want a unique identifier within the app
 */
data class ContactDataIdAndroidWithoutNo(val uuid: UUID = UUID.randomUUID()) : IContactDataIdExternal {
    override val contactDataNo: Long?
        get() = null
}

/**
 * Creates an ID for a new ContactData object.
 *
 * Use internal IDs for a start: they can be changed to external ones if the contact
 * should be stored as external;
 * but the user might change that while editing the contact, so we change as late as possible.
 */
fun createContactDataId(): ContactDataId = ContactDataIdInternal.randomId()
fun createExternalDummyContactDataId(): ContactDataId = ContactDataIdAndroidWithoutNo()
