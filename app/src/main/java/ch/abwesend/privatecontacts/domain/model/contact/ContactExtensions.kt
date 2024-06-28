/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.contact

import ch.abwesend.privatecontacts.domain.settings.Settings

fun IContact.getFullName(
    firstNameFirst: Boolean = Settings.current.orderByFirstName
): String = getFullName(firstName, lastName, nickname, middleName, namePrefix, nameSuffix, firstNameFirst)

fun getFullName(
    firstName: String,
    lastName: String,
    nickname: String,
    middleName: String,
    namePrefix: String,
    nameSuffix: String,
    firstNameFirst: Boolean = Settings.current.orderByFirstName,
): String {
    val nicknamePart = if (nickname.isBlank()) "" else " \"$nickname\" "

    val nameParts = if (firstNameFirst) {
        listOf(namePrefix, firstName, middleName, nicknamePart, lastName)
    } else {
        listOf(namePrefix, lastName, middleName, nicknamePart, firstName)
    }
    val namePartsSanitized = nameParts
        .map { it.trim() }
        .filter { it.isNotEmpty() }

    val nameWithoutSuffix = namePartsSanitized.joinToString(" ")
    val suffixPart = if (nameSuffix.isBlank()) "" else ", $nameSuffix"
    return nameWithoutSuffix + suffixPart
}

fun IContact.asEditable(): ContactEditable =
    if (this is ContactEditable) this
    else toContactEditable()

fun IContact.toContactEditable(): ContactEditable =
    ContactEditable(
        id = id,
        importId = importId,
        firstName = firstName,
        lastName = lastName,
        nickname = nickname,
        middleName = middleName,
        namePrefix = namePrefix,
        nameSuffix = nameSuffix,
        type = type,
        notes = notes,
        image = image,
        contactDataSet = contactDataSet.toMutableList(),
        contactGroups = contactGroups.toMutableList(),
        saveInAccount = ContactAccount.currentDefaultForContactType(type)
    )

fun IContact.toContactBase(): ContactBase = ContactBase(id = id, type = type, displayName = getFullName())

val IContactBase.isExternal: Boolean
    get() = id.isExternal

val ContactId.isExternal: Boolean
    get() = this is IContactIdExternal
