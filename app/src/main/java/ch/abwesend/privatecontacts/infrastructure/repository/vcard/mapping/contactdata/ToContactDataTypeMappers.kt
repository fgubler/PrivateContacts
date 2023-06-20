/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository.vcard.mapping.contactdata

import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType
import ezvcard.parameter.AddressType
import ezvcard.parameter.EmailType
import ezvcard.parameter.RelatedType
import ezvcard.parameter.TelephoneType
import ezvcard.parameter.VCardParameter
import ezvcard.property.Url
import java.util.Locale

fun TelephoneType.toContactDataType(): ContactDataType = when (this) {
    TelephoneType.CELL -> ContactDataType.Mobile
    else -> toDefaultContactDataType()
}

fun EmailType.toContactDataType(): ContactDataType = toDefaultContactDataType()

fun AddressType.toContactDataType(): ContactDataType = toDefaultContactDataType()

fun Url.toContactDataType(): ContactDataType = typeValueToContactDataType(value)

fun RelatedType.toContactDataType(): ContactDataType = when (this) {
    RelatedType.CHILD -> ContactDataType.RelationshipChild
    RelatedType.CO_WORKER -> ContactDataType.RelationshipWork
    RelatedType.COLLEAGUE -> ContactDataType.RelationshipWork
    RelatedType.FRIEND -> ContactDataType.RelationshipFriend
    RelatedType.KIN -> ContactDataType.RelationshipRelative
    RelatedType.PARENT -> ContactDataType.RelationshipParent
    RelatedType.SIBLING -> ContactDataType.RelationshipSibling
    RelatedType.SPOUSE -> ContactDataType.RelationshipPartner
    RelatedType.SWEETHEART -> ContactDataType.RelationshipPartner
    else -> toDefaultContactDataType()
}

fun List<ContactDataType>.getByPriority(): ContactDataType =
    minByOrNull { it.priority } ?: ContactDataType.Other

/**
 * The type "home" and "work" always have the same strings...
 */
private fun VCardParameter.toDefaultContactDataType(): ContactDataType = typeValueToContactDataType(value)

private fun typeValueToContactDataType(typeValue: String?): ContactDataType {
    val valueSanitized = typeValue
        .orEmpty()
        .trim()
        .lowercase()

    return when (valueSanitized) {
        TelephoneType.HOME.value.lowercase() -> ContactDataType.Personal
        TelephoneType.WORK.value.lowercase() -> ContactDataType.Business
        TelephoneType.PREF.value.lowercase() -> ContactDataType.Main
        "" -> ContactDataType.Other
        else -> ContactDataType.CustomValue(customValue = typeValue.customCapitalize())
    }
}

private fun String?.customCapitalize(): String = this?.replaceFirstChar {
    if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
}.orEmpty()
