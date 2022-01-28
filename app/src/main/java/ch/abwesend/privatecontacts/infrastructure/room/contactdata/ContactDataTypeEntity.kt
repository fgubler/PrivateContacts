package ch.abwesend.privatecontacts.infrastructure.room.contactdata

import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.Key.BIRTHDAY
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.Key.BUSINESS
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.Key.CUSTOM
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.Key.MOBILE
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.Key.OTHER
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.Key.PRIVATE

/**
 * Not a real entity but embedded...
 */
data class ContactDataTypeEntity(
    val key: ContactDataType.Key,
    val customValue: String?,
)

fun ContactDataTypeEntity.toContactDataType(): ContactDataType {
    return when (key) {
        PRIVATE -> ContactDataType.Private
        BUSINESS -> ContactDataType.Business
        MOBILE -> ContactDataType.Mobile
        OTHER -> ContactDataType.Other
        BIRTHDAY -> ContactDataType.Birthday
        CUSTOM -> ContactDataType.CustomValue(customValue.orEmpty())
    }
}

fun ContactDataType.toEntity(): ContactDataTypeEntity =
    when (this) {
        is ContactDataType.CustomValue -> ContactDataTypeEntity(key, customValue)
        else -> ContactDataTypeEntity(key, null)
    }
