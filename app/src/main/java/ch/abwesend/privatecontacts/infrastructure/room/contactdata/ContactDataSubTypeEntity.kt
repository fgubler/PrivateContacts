package ch.abwesend.privatecontacts.infrastructure.room.contactdata

import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataSubType

/**
 * Not a real entity but embedded...
 */
data class ContactDataSubTypeEntity(
    val key: String,
    val customValue: String?,
)

fun ContactDataSubTypeEntity.toContactDataSubType(): ContactDataSubType? {
    val subTypeKey = ContactDataSubType.Key.valueOf(key)
    return when (subTypeKey) {
        ContactDataSubType.Key.PRIVATE -> ContactDataSubType.Private
        ContactDataSubType.Key.BUSINESS -> ContactDataSubType.Business
        ContactDataSubType.Key.MOBILE -> ContactDataSubType.Mobile
        ContactDataSubType.Key.OTHER -> ContactDataSubType.Other
        ContactDataSubType.Key.BIRTHDAY -> ContactDataSubType.Birthday
        ContactDataSubType.Key.CUSTOM -> customValue?.let { ContactDataSubType.Custom(it) }
    }
}

fun ContactDataSubType.toEntity(): ContactDataSubTypeEntity =
    when (this) {
        is ContactDataSubType.Custom -> ContactDataSubTypeEntity(key.name, customValue)
        else -> ContactDataSubTypeEntity(key.name, null)
    }
