/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.testutil

import ch.abwesend.privatecontacts.domain.model.contact.ContactBase
import ch.abwesend.privatecontacts.domain.model.contact.ContactEditable
import ch.abwesend.privatecontacts.domain.model.contact.ContactId
import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.model.contact.ContactType.SECRET
import ch.abwesend.privatecontacts.domain.model.contact.ContactWithPhoneNumbers
import ch.abwesend.privatecontacts.domain.model.contact.IContact
import ch.abwesend.privatecontacts.domain.model.contact.IContactBase
import ch.abwesend.privatecontacts.domain.model.contact.IContactEditable
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactData
import ch.abwesend.privatecontacts.domain.model.contactdata.PhoneNumberValue
import ch.abwesend.privatecontacts.infrastructure.room.contact.ContactEntity

fun someContactId(): ContactId = ContactId.randomId()

fun someContactBase(
    id: ContactId = someContactId(),
    displayName: String = "John Snow",
    type: ContactType = SECRET,
): IContactBase = ContactBase(
    id = id,
    type = type,
    displayName = displayName,
)

fun someContactEntity(
    id: ContactId = someContactId(),
    firstName: String = "John",
    lastName: String = "Snow",
    nickname: String = "Lord Snow",
    type: ContactType = SECRET,
    notes: String = "Tries to do the right thing. Often badly.",
    fullTextSearch: String = "TestSearch",
): ContactEntity = ContactEntity(
    rawId = id.uuid,
    firstName = firstName,
    lastName = lastName,
    nickname = nickname,
    type = type,
    notes = notes,
    fullTextSearch = fullTextSearch,
)

fun someContactWithPhoneNumbers(
    id: ContactId = someContactId(),
    type: ContactType = SECRET,
    displayName: String = "John Snow",
    phoneNumbers: List<String> = emptyList(),
): ContactWithPhoneNumbers {
    val base = ContactBase(
        id = id,
        type = type,
        displayName = displayName,
    )
    return ContactWithPhoneNumbers(
        contactBase = base,
        phoneNumbers = phoneNumbers.map { PhoneNumberValue(it) }
    )
}

fun someContactEditable(
    id: ContactId = someContactId(),
    firstName: String = "John",
    lastName: String = "Snow",
    nickname: String = "Lord Snow",
    type: ContactType = SECRET,
    notes: String = "Tries to do the right thing. Often badly.",
    contactData: List<ContactData> = mutableListOf(),
    isNew: Boolean = false,
): IContactEditable = ContactEditable(
    id = id,
    firstName = firstName,
    lastName = lastName,
    nickname = nickname,
    type = type,
    notes = notes,
    contactDataSet = contactData.toMutableList(),
    isNew = isNew,
)

fun someTestContact(
    id: ContactId = someContactId(),
    firstName: String = "John",
    lastName: String = "Snow",
    nickname: String = "Lord Snow",
    type: ContactType = SECRET,
    notes: String = "Tries to do the right thing. Often badly.",
    contactData: List<ContactData> = emptyList(),
    isNew: Boolean = false,
): IContact = TestContact(
    id = id,
    firstName = firstName,
    lastName = lastName,
    nickname = nickname,
    type = type,
    notes = notes,
    contactDataSet = contactData,
    isNew = isNew,
)
