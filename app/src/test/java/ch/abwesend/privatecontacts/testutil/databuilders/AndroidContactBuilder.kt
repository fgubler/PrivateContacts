package ch.abwesend.privatecontacts.testutil.databuilders

import android.net.Uri
import com.alexstyl.contactstore.Contact
import com.alexstyl.contactstore.ContactGroup
import com.alexstyl.contactstore.EventDate
import com.alexstyl.contactstore.Label
import com.alexstyl.contactstore.LabeledValue
import com.alexstyl.contactstore.MailAddress
import com.alexstyl.contactstore.Note
import com.alexstyl.contactstore.PhoneNumber
import com.alexstyl.contactstore.PostalAddress
import com.alexstyl.contactstore.Relation
import com.alexstyl.contactstore.WebAddress
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate

fun someAndroidContact(
    contactId: Long = 123123,
    firstName: String = "Luke",
    lastName: String = "Skywalker",
    displayName: String = "$firstName $lastName",
    nickName: String = displayName,
    note: String = "daddy issues",
    phoneNumbers: List<String> = emptyList(),
    emails: List<String> = emptyList(),
    websites: List<String> = emptyList(),
    addresses: List<String> = emptyList(),
    sisters: List<String> = emptyList(),
    birthdays: List<LocalDate> = emptyList(),
    relaxed: Boolean = false,
): Contact {
    val mock = mockk<Contact>(relaxed = relaxed)

    every { mock.contactId } returns contactId
    every { mock.firstName } returns firstName
    every { mock.lastName } returns lastName
    every { mock.displayName } returns displayName
    every { mock.nickname } returns nickName
    every { mock.note } returns Note(note)
    every { mock.phones } returns phoneNumbers.map { LabeledValue(PhoneNumber(it), label = Label.PhoneNumberMobile) }
    every { mock.mails } returns emails.map { LabeledValue(MailAddress(it), label = Label.Other) }
    every { mock.webAddresses } returns websites.map {
        LabeledValue(WebAddress(someUri(it)), label = Label.WebsiteHomePage)
    }
    every { mock.postalAddresses } returns addresses.map {
        LabeledValue(PostalAddress(it), label = Label.LocationHome)
    }
    every { mock.relations } returns sisters.map { LabeledValue(Relation(it), label = Label.RelationSister) }
    every { mock.events } returns birthdays.map {
        LabeledValue(EventDate(it.dayOfMonth, it.monthValue, it.year), label = Label.DateBirthday)
    }

    return mock
}

fun someUri(path: String): Uri {
    val mock = mockk<Uri>(relaxed = true)

    every { mock.path } returns path
    every { mock.toString() } returns path

    return mock
}

fun someAndroidContactGroup(
    title: String = "SomeGroup",
    notes: String = "Just some random group",
): ContactGroup {
    val mock = mockk<ContactGroup>()

    every { mock.title } returns title
    every { mock.note } returns notes

    return mock
}