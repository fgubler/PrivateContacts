/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.room.contactdata

import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType

/**
 * Not a real entity but embedded...
 */
data class ContactDataTypeEntity(
    val key: ContactDataType.Key,
    val customValue: String?,
)

fun ContactDataTypeEntity.toContactDataType(): ContactDataType {
    return ContactDataType.fromKey(key = key, customValue = customValue)
}

fun ContactDataType.toEntity(): ContactDataTypeEntity =
    when (this) {
        is ContactDataType.CustomValue -> ContactDataTypeEntity(key, customValue)
        else -> ContactDataTypeEntity(key, null)
    }
