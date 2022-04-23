/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.room.contactdata

import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.Key.BIRTHDAY
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.Key.BUSINESS
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.Key.CUSTOM
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.Key.MAIN
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.Key.MOBILE
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.Key.OTHER
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.Key.PERSONAL

/**
 * Not a real entity but embedded...
 */
data class ContactDataTypeEntity(
    val key: ContactDataType.Key,
    val customValue: String?,
)

fun ContactDataTypeEntity.toContactDataType(): ContactDataType {
    return when (key) {
        PERSONAL -> ContactDataType.Personal
        BUSINESS -> ContactDataType.Business
        MOBILE -> ContactDataType.Mobile
        OTHER -> ContactDataType.Other
        BIRTHDAY -> ContactDataType.Birthday
        MAIN -> ContactDataType.Main
        CUSTOM -> ContactDataType.CustomValue(customValue.orEmpty())
    }
}

fun ContactDataType.toEntity(): ContactDataTypeEntity =
    when (this) {
        is ContactDataType.CustomValue -> ContactDataTypeEntity(key, customValue)
        else -> ContactDataTypeEntity(key, null)
    }
