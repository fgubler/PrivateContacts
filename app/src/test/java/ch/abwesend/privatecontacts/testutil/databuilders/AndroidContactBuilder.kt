package ch.abwesend.privatecontacts.testutil.databuilders

import android.net.Uri
import ch.abwesend.privatecontacts.domain.util.getAnywhere
import ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.factory.IAndroidContactMutableFactory
import ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.model.IAndroidContactMutable
import ch.abwesend.privatecontacts.testutil.androidcontacts.toLabeledValue
import com.alexstyl.contactstore.Contact
import com.alexstyl.contactstore.ContactGroup
import com.alexstyl.contactstore.EventDate
import com.alexstyl.contactstore.GroupMembership
import com.alexstyl.contactstore.ImageData
import com.alexstyl.contactstore.InternetAccount
import com.alexstyl.contactstore.Label
import com.alexstyl.contactstore.LabeledValue
import com.alexstyl.contactstore.MailAddress
import com.alexstyl.contactstore.Note
import com.alexstyl.contactstore.PhoneNumber
import com.alexstyl.contactstore.PostalAddress
import com.alexstyl.contactstore.Relation
import com.alexstyl.contactstore.WebAddress
import com.alexstyl.contactstore.thumbnailUri
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.spyk
import java.time.LocalDate

fun someAndroidContact(
    contactId: Long = 123123,
    firstName: String = "Luke",
    middleName: String = "",
    lastName: String = "Skywalker",
    displayName: String = "$firstName $lastName",
    nickName: String = displayName,
    note: String = "daddy issues",
    phoneNumbers: List<String> = emptyList(),
    emails: List<String> = emptyList(),
    websites: List<String> = emptyList(),
    addresses: List<String> = emptyList(),
    sisters: List<String> = emptyList(),
    additionalRelations: List<LabeledValue<Relation>> = emptyList(),
    birthdays: List<LocalDate> = emptyList(),
    organisation: String = "",
    jobTitle: String = "",
    thumbnailUri: Uri = someUri("some thumbnail uri"),
    imageData: ImageData = ImageData(ByteArray(0)),
    relaxed: Boolean = false,
): Contact {
    val mock = mockk<Contact>(relaxed = relaxed)

    mockkStatic(Contact::thumbnailUri)
    every { mock.contactId } returns contactId
    every { mock.firstName } returns firstName
    every { mock.lastName } returns lastName
    every { mock.middleName } returns middleName
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
    every { mock.relations } returns sisters
        .map { LabeledValue(Relation(it), label = Label.RelationSister) } + additionalRelations
    every { mock.events } returns birthdays.map {
        LabeledValue(EventDate(it.dayOfMonth, it.monthValue, it.year), label = Label.DateBirthday)
    }
    every { mock.thumbnailUri } returns thumbnailUri
    every { mock.imageData } returns imageData
    every { mock.organization } returns organisation
    every { mock.jobTitle } returns jobTitle

    return mock
}

fun someAndroidContactMutable(
    contactId: Long = 123123,
    firstName: String = "Luke",
    middleName: String = "",
    lastName: String = "Skywalker",
    displayName: String = "$firstName $lastName",
    nickName: String = displayName,
    note: String = "daddy issues",
    contactData: ContactDataContainer = ContactDataContainer.createEmpty(),
    organisation: String = "",
    thumbnailUri: Uri = someUri("some thumbnail uri"),
    imageData: ImageData = ImageData(ByteArray(0)),
    groups: List<GroupMembership> = emptyList(),
): IAndroidContactMutable {
    val factory = getAnywhere<IAndroidContactMutableFactory>()
    val contact = spyk(factory.create())

    mockkStatic(Contact::thumbnailUri)
    every { contact.contactId } returns contactId
    every { contact.displayName } returns displayName
    every { contact.thumbnailUri } returns thumbnailUri

    contact.firstName = firstName
    contact.lastName = lastName
    contact.middleName = middleName
    contact.nickname = nickName
    contact.note = Note(note)
    contact.phones.addAll(
        contactData.phoneNumbers.mapIndexed { index, elem ->
            PhoneNumber(elem).toLabeledValue(label = Label.PhoneNumberMobile, id = index)
        }
    )
    contact.mails.addAll(
        contactData.emailAddresses.mapIndexed { index, elem ->
            MailAddress(elem).toLabeledValue(label = Label.Other, id = index)
        }
    )
    contact.webAddresses.addAll(
        contactData.websites.mapIndexed { index, elem ->
            WebAddress(someUri(elem)).toLabeledValue(label = Label.WebsiteHomePage, id = index)
        }
    )
    contact.postalAddresses.addAll(
        contactData.physicalAddresses.mapIndexed { index, elem ->
            PostalAddress(elem).toLabeledValue(label = Label.LocationHome, id = index)
        }
    )
    contact.relations.addAll(
        contactData.relationships.mapIndexed { index, elem ->
            Relation(elem).toLabeledValue(label = Label.RelationSister, id = index)
        }
    )
    contact.events.addAll(
        contactData.eventDates.mapIndexed { index, elem ->
            EventDate(elem.dayOfMonth, elem.monthValue, elem.year)
                .toLabeledValue(label = Label.DateBirthday, id = index)
        }
    )
    contact.groups.addAll(groups)
    contact.imageData = imageData
    contact.organization = organisation

    return contact
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
    groupId: Long = 123,
    account: InternetAccount? = someInternetAccount(),
): ContactGroup {
    val mock = mockk<ContactGroup>()

    every { mock.title } returns title
    every { mock.note } returns notes
    every { mock.groupId } returns groupId
    every { mock.account } returns account

    return mock
}

fun someInternetAccount(name: String = "alpha@beta.ch", type: String = "google.com"): InternetAccount =
    InternetAccount(name, type)

data class ContactDataContainer(
    val phoneNumbers: List<String>,
    val emailAddresses: List<String>,
    val physicalAddresses: List<String>,
    val websites: List<String>,
    val relationships: List<String>,
    val eventDates: List<LocalDate>,
    val companies: List<String>,
) {
    companion object {
        fun createEmpty(): ContactDataContainer =
            ContactDataContainer(
                phoneNumbers = emptyList(),
                emailAddresses = emptyList(),
                physicalAddresses = emptyList(),
                websites = emptyList(),
                relationships = emptyList(),
                eventDates = emptyList(),
                companies = emptyList(),
            )
    }
}
