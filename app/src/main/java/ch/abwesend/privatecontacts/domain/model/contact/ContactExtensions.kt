/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.contact

import ch.abwesend.privatecontacts.domain.settings.Settings

fun IContact.getFullName(
    firstNameFirst: Boolean = Settings.current.orderByFirstName
): String = getFullName(category, firstName, lastName, nickname, organizationName, firstNameFirst)

fun getFullName(
    category: ContactCategory,
    firstName: String,
    lastName: String,
    nickname: String,
    organizationName: String,
    firstNameFirst: Boolean = Settings.current.orderByFirstName,
): String = when (category) {
    ContactCategory.PERSON -> getFullPersonName(firstName, lastName, nickname, firstNameFirst)
        .ifEmpty { organizationName } // just as fallback...
    ContactCategory.ORGANIZATION ->
        organizationName
            .ifEmpty { getFullPersonName(firstName, lastName, nickname, firstNameFirst) }
}

private fun getFullPersonName(
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
        organizationName = organizationName,
        type = type,
        category = category,
        notes = notes,
        image = image,
        contactDataSet = contactDataSet.toMutableList(),
        contactGroups = contactGroups.toMutableList(),
        saveInAccount = ContactAccount.currentDefaultForContactType(type)
    )

fun IContact.toContactBase(): ContactBase =
    ContactBase(
        id = id,
        type = type,
        displayName = getFullName(category, firstName, lastName, nickname, organizationName),
    )

val IContactBase.isExternal: Boolean
    get() = id.isExternal

val ContactId.isExternal: Boolean
    get() = this is IContactIdExternal
