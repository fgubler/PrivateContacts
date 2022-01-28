package ch.abwesend.privatecontacts.domain.model.contact

import ch.abwesend.privatecontacts.domain.Settings
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactData
import java.util.UUID

fun ContactBase.getFullName(
    firstNameFirst: Boolean = Settings.orderByFirstName
): String =
    if (firstNameFirst) "$firstName $lastName"
    else "$lastName $firstName"

fun Contact.asFull(): ContactFull =
    if (this is ContactFull) this
    else toContactFull(this.contactDataSet.toMutableList())

fun ContactBase.toContactFull(
    contactDataSet: List<ContactData>
): ContactFull =
    ContactFull(
        id = id,
        firstName = firstName,
        lastName = lastName,
        nickname = nickname,
        type = type,
        notes = notes,
        contactDataSet = contactDataSet,
    )

fun ContactFull.Companion.createNew(): ContactFull =
    ContactFull(
        id = UUID.randomUUID(),
        firstName = "",
        lastName = "",
        nickname = "",
        type = ContactType.PRIVATE,
        notes = "",
        contactDataSet = listOf(),
        isNew = true,
    )
