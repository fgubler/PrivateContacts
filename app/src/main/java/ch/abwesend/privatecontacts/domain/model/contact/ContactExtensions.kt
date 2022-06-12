/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.contact

import ch.abwesend.privatecontacts.domain.settings.Settings

fun IContactBase.getFullName(
    firstNameFirst: Boolean = Settings.current.orderByFirstName
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
    )

val IContactBase.isExternal: Boolean
    get() = id is IContactIdExternal
