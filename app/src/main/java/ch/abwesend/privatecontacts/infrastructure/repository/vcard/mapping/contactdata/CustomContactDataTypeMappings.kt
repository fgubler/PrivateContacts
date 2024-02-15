/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository.vcard.mapping.contactdata

import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType

private const val CONTACT_TYPE_PERSONAL_STRING = "private"
private const val CONTACT_TYPE_BUSINESS_STRING = "work"
private const val CONTACT_TYPE_MOBILE_STRING = "mobile"
private const val CONTACT_TYPE_MOBILE_BUSINESS_STRING = "mobile_business"
private const val CONTACT_TYPE_OTHER_STRING = "other"
private const val CONTACT_TYPE_BIRTHDAY_STRING = "birthday"
private const val CONTACT_TYPE_ANNIVERSARY_STRING = "anniversary"
private const val CONTACT_TYPE_MAIN_STRING = "main"
private const val CONTACT_TYPE_SIBLING_STRING = "sibling"
private const val CONTACT_TYPE_BROTHER_STRING = "brother"
private const val CONTACT_TYPE_SISTER_STRING = "sister"
private const val CONTACT_TYPE_PARENT_STRING = "parent"
private const val CONTACT_TYPE_FATHER_STRING = "father"
private const val CONTACT_TYPE_MOTHER_STRING = "mother"
private const val CONTACT_TYPE_CHILD_STRING = "child"
private const val CONTACT_TYPE_RELATIVE_STRING = "relative"
private const val CONTACT_TYPE_PARTNER_STRING = "partner"
private const val CONTACT_TYPE_FRIEND_STRING = "friend"
private const val CONTACT_TYPE_COLLEAGUE_STRING = "colleague"

fun getVCardStringByContactDataType(type: ContactDataType): String = when (type) {
    is ContactDataType.Personal -> CONTACT_TYPE_PERSONAL_STRING
    is ContactDataType.Business -> CONTACT_TYPE_BUSINESS_STRING
    is ContactDataType.Mobile -> CONTACT_TYPE_MOBILE_STRING
    is ContactDataType.MobileBusiness -> CONTACT_TYPE_MOBILE_BUSINESS_STRING
    is ContactDataType.Other -> CONTACT_TYPE_OTHER_STRING
    is ContactDataType.Birthday -> CONTACT_TYPE_BIRTHDAY_STRING
    is ContactDataType.Anniversary -> CONTACT_TYPE_ANNIVERSARY_STRING
    is ContactDataType.Main -> CONTACT_TYPE_MAIN_STRING
    is ContactDataType.Custom -> CONTACT_TYPE_OTHER_STRING // this should never happen (just a place-holder)
    is ContactDataType.CustomValue -> type.customValue
    is ContactDataType.RelationshipFriend -> CONTACT_TYPE_FRIEND_STRING
    is ContactDataType.RelationshipParent -> CONTACT_TYPE_PARENT_STRING
    is ContactDataType.RelationshipFather -> CONTACT_TYPE_FATHER_STRING
    is ContactDataType.RelationshipMother -> CONTACT_TYPE_MOTHER_STRING
    is ContactDataType.RelationshipChild -> CONTACT_TYPE_CHILD_STRING
    is ContactDataType.RelationshipPartner -> CONTACT_TYPE_PARTNER_STRING
    is ContactDataType.RelationshipRelative -> CONTACT_TYPE_RELATIVE_STRING
    is ContactDataType.RelationshipSibling -> CONTACT_TYPE_SIBLING_STRING
    is ContactDataType.RelationshipBrother -> CONTACT_TYPE_BROTHER_STRING
    is ContactDataType.RelationshipSister -> CONTACT_TYPE_SISTER_STRING
    is ContactDataType.RelationshipWork -> CONTACT_TYPE_COLLEAGUE_STRING
}

/**
 * @return the corresponding [ContactDataType] if the [vCardString] is a pre-defined custom-key for one; null otherwise
 */
fun getContactDataTypeByCustomVCardString(vCardString: String): ContactDataType? = when (vCardString) {
    CONTACT_TYPE_PERSONAL_STRING -> ContactDataType.Personal
    CONTACT_TYPE_BUSINESS_STRING -> ContactDataType.Business
    CONTACT_TYPE_MOBILE_STRING -> ContactDataType.Mobile
    CONTACT_TYPE_MOBILE_BUSINESS_STRING -> ContactDataType.MobileBusiness
    CONTACT_TYPE_OTHER_STRING -> ContactDataType.Other
    CONTACT_TYPE_BIRTHDAY_STRING -> ContactDataType.Birthday
    CONTACT_TYPE_ANNIVERSARY_STRING -> ContactDataType.Anniversary
    CONTACT_TYPE_MAIN_STRING -> ContactDataType.Main
    CONTACT_TYPE_FRIEND_STRING -> ContactDataType.RelationshipFriend
    CONTACT_TYPE_PARENT_STRING -> ContactDataType.RelationshipParent
    CONTACT_TYPE_FATHER_STRING -> ContactDataType.RelationshipFather
    CONTACT_TYPE_MOTHER_STRING -> ContactDataType.RelationshipMother
    CONTACT_TYPE_CHILD_STRING -> ContactDataType.RelationshipChild
    CONTACT_TYPE_PARTNER_STRING -> ContactDataType.RelationshipPartner
    CONTACT_TYPE_RELATIVE_STRING -> ContactDataType.RelationshipRelative
    CONTACT_TYPE_SIBLING_STRING -> ContactDataType.RelationshipSibling
    CONTACT_TYPE_BROTHER_STRING -> ContactDataType.RelationshipBrother
    CONTACT_TYPE_SISTER_STRING -> ContactDataType.RelationshipSister
    CONTACT_TYPE_COLLEAGUE_STRING -> ContactDataType.RelationshipWork
    else -> null
}
