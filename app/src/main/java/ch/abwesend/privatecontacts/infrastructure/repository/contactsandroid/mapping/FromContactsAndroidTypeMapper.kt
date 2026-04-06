/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository.contactsandroid.mapping

import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType
import contacts.core.entities.AddressEntity
import contacts.core.entities.EmailEntity
import contacts.core.entities.EventEntity
import contacts.core.entities.PhoneEntity
import contacts.core.entities.RelationEntity

// ── Phone ──────────────────────────────────────────────────────────────────────

fun PhoneEntity.Type?.toContactDataType(label: String?): ContactDataType =
    when (this) {
        PhoneEntity.Type.MOBILE -> ContactDataType.Mobile
        PhoneEntity.Type.HOME -> ContactDataType.Personal
        PhoneEntity.Type.WORK -> ContactDataType.Business
        PhoneEntity.Type.MAIN -> ContactDataType.Main
        PhoneEntity.Type.COMPANY_MAIN -> ContactDataType.Business
        PhoneEntity.Type.WORK_MOBILE -> ContactDataType.MobileBusiness
        PhoneEntity.Type.CUSTOM -> label?.let { ContactDataType.CustomValue(it) } ?: ContactDataType.Other
        PhoneEntity.Type.FAX_WORK,
        PhoneEntity.Type.FAX_HOME,
        PhoneEntity.Type.PAGER,
        PhoneEntity.Type.OTHER,
        PhoneEntity.Type.CALLBACK,
        PhoneEntity.Type.CAR,
        PhoneEntity.Type.ISDN,
        PhoneEntity.Type.OTHER_FAX,
        PhoneEntity.Type.RADIO,
        PhoneEntity.Type.TELEX,
        PhoneEntity.Type.TTY_TDD,
        PhoneEntity.Type.WORK_PAGER,
        PhoneEntity.Type.ASSISTANT,
        PhoneEntity.Type.MMS,
        null -> ContactDataType.Other
    }

// ── Email ──────────────────────────────────────────────────────────────────────

fun EmailEntity.Type?.toContactDataType(label: String?): ContactDataType =
    when (this) {
        EmailEntity.Type.HOME -> ContactDataType.Personal
        EmailEntity.Type.WORK -> ContactDataType.Business
        EmailEntity.Type.OTHER -> ContactDataType.Other
        EmailEntity.Type.CUSTOM -> label?.let { ContactDataType.CustomValue(it) } ?: ContactDataType.Other
        null -> ContactDataType.Other
    }

// ── Address ────────────────────────────────────────────────────────────────────

fun AddressEntity.Type?.toContactDataType(label: String?): ContactDataType =
    when (this) {
        AddressEntity.Type.HOME -> ContactDataType.Personal
        AddressEntity.Type.WORK -> ContactDataType.Business
        AddressEntity.Type.OTHER -> ContactDataType.Other
        AddressEntity.Type.CUSTOM -> label?.let { ContactDataType.CustomValue(it) } ?: ContactDataType.Other
        null -> ContactDataType.Other
    }

// ── Event ──────────────────────────────────────────────────────────────────────

fun EventEntity.Type?.toContactDataType(label: String?): ContactDataType =
    when (this) {
        EventEntity.Type.BIRTHDAY -> ContactDataType.Birthday
        EventEntity.Type.ANNIVERSARY -> ContactDataType.Anniversary
        EventEntity.Type.OTHER -> ContactDataType.Other
        EventEntity.Type.CUSTOM -> label?.let { ContactDataType.CustomValue(it) } ?: ContactDataType.Other
        null -> ContactDataType.Other
    }

// ── Relation ───────────────────────────────────────────────────────────────────

fun RelationEntity.Type?.toContactDataType(label: String?): ContactDataType =
    when (this) {
        RelationEntity.Type.BROTHER -> ContactDataType.RelationshipBrother
        RelationEntity.Type.SISTER -> ContactDataType.RelationshipSister
        RelationEntity.Type.CHILD -> ContactDataType.RelationshipChild
        RelationEntity.Type.FATHER -> ContactDataType.RelationshipFather
        RelationEntity.Type.MOTHER -> ContactDataType.RelationshipMother
        RelationEntity.Type.PARENT -> ContactDataType.RelationshipParent
        RelationEntity.Type.PARTNER -> ContactDataType.RelationshipPartner
        RelationEntity.Type.DOMESTIC_PARTNER -> ContactDataType.RelationshipPartner
        RelationEntity.Type.RELATIVE -> ContactDataType.RelationshipRelative
        RelationEntity.Type.FRIEND -> ContactDataType.RelationshipFriend
        RelationEntity.Type.MANAGER -> ContactDataType.RelationshipWork
        RelationEntity.Type.ASSISTANT -> ContactDataType.RelationshipWork
        RelationEntity.Type.SPOUSE -> ContactDataType.RelationshipPartner
        RelationEntity.Type.REFERRED_BY -> ContactDataType.Other
        RelationEntity.Type.CUSTOM -> label?.let { ContactDataType.CustomValue(it) } ?: ContactDataType.Other
        null -> ContactDataType.Other
    }
