package ch.abwesend.privatecontacts.testutil

import ch.abwesend.privatecontacts.domain.model.contact.Contact
import ch.abwesend.privatecontacts.domain.model.contact.ContactBase
import ch.abwesend.privatecontacts.domain.model.contact.ContactFull
import ch.abwesend.privatecontacts.domain.model.contact.ContactLite
import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.model.contact.ContactType.PRIVATE
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactData
import io.mockk.every
import io.mockk.mockk
import java.util.UUID

fun someContactBase(
    id: UUID = UUID.randomUUID(),
    firstName: String = "John",
    lastName: String = "Snow",
    nickname: String = "Lord Snow",
    type: ContactType = PRIVATE,
    notes: String = "Tries to do the right thing. Often badly.",
): ContactBase = ContactLite(
    id = id,
    firstName = firstName,
    lastName = lastName,
    nickname = nickname,
    type = type,
    notes = notes,
)

fun someContactFull(
    id: UUID = UUID.randomUUID(),
    firstName: String = "John",
    lastName: String = "Snow",
    nickname: String = "Lord Snow",
    type: ContactType = PRIVATE,
    notes: String = "Tries to do the right thing. Often badly.",
    contactData: List<ContactData> = emptyList(),
    isNew: Boolean = false,
): Contact = ContactFull(
    id = id,
    firstName = firstName,
    lastName = lastName,
    nickname = nickname,
    type = type,
    notes = notes,
    contactDataSet = contactData,
    isNew = isNew,
)

fun someContactNonEditable(
    id: UUID = UUID.randomUUID(),
    firstName: String = "John",
    lastName: String = "Snow",
    nickname: String = "Lord Snow",
    type: ContactType = PRIVATE,
    notes: String = "Tries to do the right thing. Often badly.",
    contactDataSet: List<ContactData> = emptyList(),
): Contact {
    val mock = mockk<Contact>()

    every { mock.id } returns id
    every { mock.firstName } returns firstName
    every { mock.lastName } returns lastName
    every { mock.nickname } returns nickname
    every { mock.type } returns type
    every { mock.notes } returns notes
    every { mock.contactDataSet } returns contactDataSet

    return mock
}
