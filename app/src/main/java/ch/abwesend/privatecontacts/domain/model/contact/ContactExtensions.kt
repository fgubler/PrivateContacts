package ch.abwesend.privatecontacts.domain.model.contact

import ch.abwesend.privatecontacts.domain.model.contactdata.PhoneNumber
import java.util.UUID

fun ContactBase.getFullName(firstNameFirst: Boolean): String =
    if (firstNameFirst) "$firstName $lastName"
    else "$lastName $firstName"

fun Contact.asFull(): ContactFull =
    if (this is ContactFull) this
    else toContactEditable(this.phoneNumbers.toMutableList())

fun ContactBase.toContactEditable(
    phoneNumbers: MutableList<PhoneNumber>
): ContactFull =
    ContactFull(
        id = id,
        firstName = firstName,
        lastName = lastName,
        nickname = nickname,
        type = type,
        notes = notes,
        phoneNumbers = phoneNumbers,
    )

fun ContactFull.Companion.createNew(): ContactFull =
    ContactFull(
        id = UUID.randomUUID(),
        firstName = "",
        lastName = "",
        nickname = "",
        type = ContactType.PUBLIC,
        notes = "",
        phoneNumbers = mutableListOf(),
        isNew = true,
    )
