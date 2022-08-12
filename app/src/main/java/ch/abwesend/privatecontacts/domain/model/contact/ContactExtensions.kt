/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.contact

import ch.abwesend.privatecontacts.domain.settings.Settings

fun IContact.getFullName(
    firstNameFirst: Boolean = Settings.current.orderByFirstName
): String = getFullName(firstName, lastName, firstNameFirst)

fun getFullName(
    firstName: String,
    lastName: String,
    firstNameFirst: Boolean = Settings.current.orderByFirstName,
): String =
    if (firstNameFirst) "$firstName $lastName"
    else "$lastName $firstName"

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
        contactDataSet = contactDataSet.toMutableList(),
        contactGroups = contactGroups.toMutableList(),
    )

val IContactBase.isExternal: Boolean
    get() = id.isExternal

val ContactId.isExternal: Boolean
    get() = this is IContactIdExternal
