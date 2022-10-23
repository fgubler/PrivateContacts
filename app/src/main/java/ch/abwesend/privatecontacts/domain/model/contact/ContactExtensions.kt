/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.contact

import ch.abwesend.privatecontacts.domain.settings.Settings

fun IContact.getFullName(
    firstNameFirst: Boolean = Settings.current.orderByFirstName
): String = getFullName(firstName, lastName, nickname, firstNameFirst)

fun getFullName(
    firstName: String,
    lastName: String,
    nickname: String,
    firstNameFirst: Boolean = Settings.current.orderByFirstName,
): String {
    val middlePart = if (nickname.isBlank()) " " else " \"$nickname\" "
    return if (firstNameFirst) "$firstName$middlePart$lastName"
    else "$lastName$middlePart$firstName"
}

fun IContact.asEditable(): ContactEditable =
    if (this is ContactEditable) this
    else toContactEditable()

fun IContact.toContactEditable(): ContactEditable =
    ContactEditable(
        id = id,
        firstName = firstName,
        lastName = lastName,
        nickname = nickname,
        type = type,
        notes = notes,
        image = image,
        contactDataSet = contactDataSet.toMutableList(),
        contactGroups = contactGroups.toMutableList(),
    )

fun IContact.toContactBase(): ContactBase =
    ContactBase(
        id = id,
        type = type,
        displayName = getFullName(firstName, lastName, nickname),
    )

val IContactBase.isExternal: Boolean
    get() = id.isExternal

val ContactId.isExternal: Boolean
    get() = this is IContactIdExternal
