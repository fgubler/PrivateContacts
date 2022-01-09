package ch.abwesend.privatecontacts.infrastructure.room.contactdata

import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataSubType

/**
 * Not a real entity but embedded...
 */
data class ContactDataSubTypeEntity(
    val key: ContactDataSubType.Key,
    val customValue: String?,
)

fun ContactDataSubTypeEntity.toContactDataSubType(): ContactDataSubType {
    return when (key) {
        ContactDataSubType.Key.PRIVATE -> ContactDataSubType.Private
        ContactDataSubType.Key.BUSINESS -> ContactDataSubType.Business
        ContactDataSubType.Key.MOBILE -> ContactDataSubType.Mobile
        ContactDataSubType.Key.OTHER -> ContactDataSubType.Other
        ContactDataSubType.Key.BIRTHDAY -> ContactDataSubType.Birthday
        ContactDataSubType.Key.CUSTOM -> ContactDataSubType.CustomValue(customValue.orEmpty())
    }
}

fun ContactDataSubType.toEntity(): ContactDataSubTypeEntity =
    when (this) {
        is ContactDataSubType.CustomValue -> ContactDataSubTypeEntity(key, customValue)
        else -> ContactDataSubTypeEntity(key, null)
    }
