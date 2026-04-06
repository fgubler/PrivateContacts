/*
 * Private Contacts
 * Copyright (c) 2026.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.testutil.databuilders

import android.accounts.Account
import android.net.Uri
import contacts.core.entities.Address
import contacts.core.entities.AddressEntity
import contacts.core.entities.Contact
import contacts.core.entities.Email
import contacts.core.entities.EmailEntity
import contacts.core.entities.Event
import contacts.core.entities.EventDate
import contacts.core.entities.EventEntity
import contacts.core.entities.Group
import contacts.core.entities.Name
import contacts.core.entities.Nickname
import contacts.core.entities.Note
import contacts.core.entities.Organization
import contacts.core.entities.Phone
import contacts.core.entities.PhoneEntity
import contacts.core.entities.RawContact
import contacts.core.entities.Relation
import contacts.core.entities.RelationEntity
import contacts.core.entities.Website
import io.mockk.every
import io.mockk.mockk

fun someContactsAndroidContact(
    id: Long = 100L,
    lookupKey: String? = "lookup-key-100",
    displayNamePrimary: String? = "Luke Skywalker",
    photoThumbnailUri: Uri? = null,
    rawContacts: List<RawContact> = listOf(someContactsAndroidRawContact()),
): Contact {
    val mock = mockk<Contact>(relaxed = true)
    every { mock.id } returns id
    every { mock.lookupKey } returns lookupKey
    every { mock.displayNamePrimary } returns displayNamePrimary
    every { mock.photoThumbnailUri } returns photoThumbnailUri
    every { mock.rawContacts } returns rawContacts
    return mock
}

fun someContactsAndroidRawContact(
    id: Long = 200L,
    contactId: Long = 100L,
    name: Name? = null,
    nickname: Nickname? = null,
    note: Note? = null,
    organization: Organization? = null,
    phones: List<Phone> = emptyList(),
    emails: List<Email> = emptyList(),
    addresses: List<Address> = emptyList(),
    websites: List<Website> = emptyList(),
    relations: List<Relation> = emptyList(),
    events: List<Event> = emptyList(),
): RawContact {
    val mock = mockk<RawContact>(relaxed = true)
    every { mock.id } returns id
    every { mock.contactId } returns contactId
    every { mock.name } returns name
    every { mock.nickname } returns nickname
    every { mock.note } returns note
    every { mock.organization } returns organization
    every { mock.phones } returns phones
    every { mock.emails } returns emails
    every { mock.addresses } returns addresses
    every { mock.websites } returns websites
    every { mock.relations } returns relations
    every { mock.events } returns events
    return mock
}

fun someContactsAndroidName(
    givenName: String? = "Luke",
    familyName: String? = "Skywalker",
    middleName: String? = "",
    prefix: String? = "",
    suffix: String? = "",
): Name {
    val mock = mockk<Name>(relaxed = true)
    every { mock.givenName } returns givenName
    every { mock.familyName } returns familyName
    every { mock.middleName } returns middleName
    every { mock.prefix } returns prefix
    every { mock.suffix } returns suffix
    return mock
}

fun someContactsAndroidNickname(name: String? = "Lukey"): Nickname {
    val mock = mockk<Nickname>(relaxed = true)
    every { mock.name } returns name
    return mock
}

fun someContactsAndroidNote(note: String? = "some note"): Note {
    val mock = mockk<Note>(relaxed = true)
    every { mock.note } returns note
    return mock
}

fun someContactsAndroidPhone(
    id: Long = 1L,
    number: String? = "+41791234567",
    type: PhoneEntity.Type? = PhoneEntity.Type.MOBILE,
    label: String? = null,
    isPrimary: Boolean = false,
    isSuperPrimary: Boolean = false,
): Phone {
    val mock = mockk<Phone>(relaxed = true)
    every { mock.id } returns id
    every { mock.idOrNull } returns id
    every { mock.number } returns number
    every { mock.type } returns type
    every { mock.label } returns label
    every { mock.isPrimary } returns isPrimary
    every { mock.isSuperPrimary } returns isSuperPrimary
    return mock
}

fun someContactsAndroidEmail(
    id: Long = 2L,
    address: String? = "luke@rebels.org",
    type: EmailEntity.Type? = EmailEntity.Type.HOME,
    label: String? = null,
    isPrimary: Boolean = false,
    isSuperPrimary: Boolean = false,
): Email {
    val mock = mockk<Email>(relaxed = true)
    every { mock.id } returns id
    every { mock.idOrNull } returns id
    every { mock.address } returns address
    every { mock.type } returns type
    every { mock.label } returns label
    every { mock.isPrimary } returns isPrimary
    every { mock.isSuperPrimary } returns isSuperPrimary
    return mock
}

fun someContactsAndroidAddress(
    id: Long = 3L,
    street: String? = "123 Main St",
    city: String? = "Mos Eisley",
    region: String? = "Tatooine",
    postcode: String? = "12345",
    country: String? = "Galactic Republic",
    neighborhood: String? = "",
    type: AddressEntity.Type? = AddressEntity.Type.HOME,
    label: String? = null,
    isPrimary: Boolean = false,
    isSuperPrimary: Boolean = false,
): Address {
    val mock = mockk<Address>(relaxed = true)
    every { mock.id } returns id
    every { mock.idOrNull } returns id
    every { mock.street } returns street
    every { mock.city } returns city
    every { mock.region } returns region
    every { mock.postcode } returns postcode
    every { mock.country } returns country
    every { mock.neighborhood } returns neighborhood
    every { mock.type } returns type
    every { mock.label } returns label
    every { mock.isPrimary } returns isPrimary
    every { mock.isSuperPrimary } returns isSuperPrimary
    return mock
}

fun someContactsAndroidWebsite(
    id: Long = 4L,
    url: String? = "https://rebels.org",
    isPrimary: Boolean = false,
    isSuperPrimary: Boolean = false,
): Website {
    val mock = mockk<Website>(relaxed = true)
    every { mock.id } returns id
    every { mock.idOrNull } returns id
    every { mock.url } returns url
    every { mock.isPrimary } returns isPrimary
    every { mock.isSuperPrimary } returns isSuperPrimary
    return mock
}

fun someContactsAndroidRelation(
    id: Long = 5L,
    name: String? = "Leia",
    type: RelationEntity.Type? = RelationEntity.Type.SISTER,
    label: String? = null,
    isPrimary: Boolean = false,
    isSuperPrimary: Boolean = false,
): Relation {
    val mock = mockk<Relation>(relaxed = true)
    every { mock.id } returns id
    every { mock.idOrNull } returns id
    every { mock.name } returns name
    every { mock.type } returns type
    every { mock.label } returns label
    every { mock.isPrimary } returns isPrimary
    every { mock.isSuperPrimary } returns isSuperPrimary
    return mock
}

fun someContactsAndroidEvent(
    id: Long = 6L,
    date: EventDate? = null,
    type: EventEntity.Type? = EventEntity.Type.BIRTHDAY,
    label: String? = null,
    isPrimary: Boolean = false,
    isSuperPrimary: Boolean = false,
): Event {
    val mock = mockk<Event>(relaxed = true)
    every { mock.id } returns id
    every { mock.idOrNull } returns id
    every { mock.date } returns date
    every { mock.type } returns type
    every { mock.label } returns label
    every { mock.isPrimary } returns isPrimary
    every { mock.isSuperPrimary } returns isSuperPrimary
    return mock
}

fun someContactsAndroidEventDate(
    year: Int? = 1990,
    month: Int = 5,
    dayOfMonth: Int = 15,
): EventDate {
    val mock = mockk<EventDate>(relaxed = true)
    every { mock.year } returns year
    every { mock.month } returns month
    every { mock.dayOfMonth } returns dayOfMonth
    return mock
}

fun someContactsAndroidOrganization(
    id: Long = 7L,
    company: String? = "Rebel Alliance",
    title: String? = null,
    department: String? = null,
    isPrimary: Boolean = false,
    isSuperPrimary: Boolean = false,
): Organization {
    val mock = mockk<Organization>(relaxed = true)
    every { mock.id } returns id
    every { mock.idOrNull } returns id
    every { mock.company } returns company
    every { mock.title } returns title
    every { mock.department } returns department
    every { mock.isPrimary } returns isPrimary
    every { mock.isSuperPrimary } returns isSuperPrimary
    return mock
}

fun someContactsAndroidGroup(
    id: Long = 10L,
    title: String = "Friends",
    account: Account? = null,
): Group {
    val mock = mockk<Group>(relaxed = true)
    every { mock.id } returns id
    every { mock.title } returns title
    every { mock.account } returns account
    return mock
}
