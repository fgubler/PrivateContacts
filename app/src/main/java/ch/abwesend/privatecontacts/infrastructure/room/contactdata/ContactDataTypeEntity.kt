/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.room.contactdata

import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.Key.Anniversary
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.Key.BIRTHDAY
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.Key.BUSINESS
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.Key.CUSTOM
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.Key.MAIN
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.Key.MOBILE
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.Key.MOBILE_BUSINESS
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.Key.OTHER
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.Key.PERSONAL
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.Key.RELATIONSHIP_CHILD
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.Key.RELATIONSHIP_FRIEND
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.Key.RELATIONSHIP_PARENT
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.Key.RELATIONSHIP_PARTNER
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.Key.RELATIONSHIP_RELATIVE
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.Key.RELATIONSHIP_SIBLING
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.Key.RELATIONSHIP_WORK

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
        MOBILE_BUSINESS -> ContactDataType.MobileBusiness
        OTHER -> ContactDataType.Other
        BIRTHDAY -> ContactDataType.Birthday
        Anniversary -> ContactDataType.Anniversary
        MAIN -> ContactDataType.Main
        CUSTOM -> ContactDataType.CustomValue(customValue.orEmpty())

        RELATIONSHIP_SIBLING -> ContactDataType.RelationshipSibling
        RELATIONSHIP_PARENT -> ContactDataType.RelationshipParent
        RELATIONSHIP_CHILD -> ContactDataType.RelationshipChild
        RELATIONSHIP_RELATIVE -> ContactDataType.RelationshipRelative
        RELATIONSHIP_PARTNER -> ContactDataType.RelationshipPartner
        RELATIONSHIP_FRIEND -> ContactDataType.RelationshipFriend
        RELATIONSHIP_WORK -> ContactDataType.RelationshipWork
    }
}

fun ContactDataType.toEntity(): ContactDataTypeEntity =
    when (this) {
        is ContactDataType.CustomValue -> ContactDataTypeEntity(key, customValue)
        else -> ContactDataTypeEntity(key, null)
    }
