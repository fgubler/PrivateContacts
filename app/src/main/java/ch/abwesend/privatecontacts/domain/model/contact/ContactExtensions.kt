package ch.abwesend.privatecontacts.domain.model.contact

import ch.abwesend.privatecontacts.domain.model.contactdata.PhoneNumber
import java.util.UUID

fun ContactBase.getFullName(firstNameFirst: Boolean): String =
    if (firstNameFirst) "$firstName $lastName"
    else "$lastName $firstName"

fun Contact.asEditable(): ContactEditable =
    if (this is ContactEditable) this
    else toContactEditable(this.phoneNumbers.toMutableList())

fun ContactBase.toContactEditable(
    phoneNumbers: MutableList<PhoneNumber>
): ContactEditable =
    ContactEditable(
        id = id,
        firstName = firstName,
        lastName = lastName,
        nickname = nickname,
        type = type,
        notes = notes,
        phoneNumbers = phoneNumbers,
    )

fun ContactEditable.Companion.createNew(): ContactEditable =
    ContactEditable(
        id = UUID.randomUUID(),
        firstName = "",
        lastName = "",
        nickname = "",
        type = ContactType.PUBLIC,
        notes = "",
        phoneNumbers = mutableListOf(),
        isNew = true,
    )
