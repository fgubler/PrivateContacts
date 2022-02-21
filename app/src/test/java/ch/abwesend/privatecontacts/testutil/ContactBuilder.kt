/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.testutil

import ch.abwesend.privatecontacts.domain.model.contact.Contact
import ch.abwesend.privatecontacts.domain.model.contact.ContactBase
import ch.abwesend.privatecontacts.domain.model.contact.ContactEditable
import ch.abwesend.privatecontacts.domain.model.contact.ContactId
import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.model.contact.ContactType.PRIVATE
import ch.abwesend.privatecontacts.domain.model.contact.IContact
import ch.abwesend.privatecontacts.domain.model.contact.IContactBase
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactData

fun someContactId(): ContactId = ContactId.randomId()

fun someContactBase(
    id: ContactId = someContactId(),
    firstName: String = "John",
    lastName: String = "Snow",
    nickname: String = "Lord Snow",
    type: ContactType = PRIVATE,
    notes: String = "Tries to do the right thing. Often badly.",
): IContactBase = ContactBase(
    id = id,
    firstName = firstName,
    lastName = lastName,
    nickname = nickname,
    type = type,
    notes = notes,
)

fun someContactFull(
    id: ContactId = someContactId(),
    firstName: String = "John",
    lastName: String = "Snow",
    nickname: String = "Lord Snow",
    type: ContactType = PRIVATE,
    notes: String = "Tries to do the right thing. Often badly.",
    contactData: List<ContactData> = emptyList(),
    isNew: Boolean = false,
): IContact = Contact(
    id = id,
    firstName = firstName,
    lastName = lastName,
    nickname = nickname,
    type = type,
    notes = notes,
    contactDataSet = contactData,
    isNew = isNew,
)

fun someContactEditable(
    id: ContactId = someContactId(),
    firstName: String = "John",
    lastName: String = "Snow",
    nickname: String = "Lord Snow",
    type: ContactType = PRIVATE,
    notes: String = "Tries to do the right thing. Often badly.",
    contactData: MutableList<ContactData> = mutableListOf(),
    isNew: Boolean = false,
): IContact = ContactEditable(
    id = id,
    firstName = firstName,
    lastName = lastName,
    nickname = nickname,
    type = type,
    notes = notes,
    contactDataSet = contactData,
    isNew = isNew,
)
