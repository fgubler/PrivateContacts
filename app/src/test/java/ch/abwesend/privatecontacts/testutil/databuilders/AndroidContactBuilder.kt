package ch.abwesend.privatecontacts.testutil.databuilders

import com.alexstyl.contactstore.Contact
import com.alexstyl.contactstore.Note
import io.mockk.MockKSettings.relaxed
import io.mockk.every
import io.mockk.mockk

fun someAndroidContact(
    contactId: Long = 123123,
    firstName: String = "Luke",
    lastName: String = "Skywalker",
    displayName: String = "$firstName $lastName",
    nickName: String = displayName,
    note: String = "daddy issues",
    relaxed: Boolean = false,
): Contact {
    val mock = mockk<Contact>(relaxed = relaxed)

    every { mock.contactId } returns contactId
    every { mock.firstName } returns firstName
    every { mock.lastName } returns lastName
    every { mock.displayName } returns displayName
    every { mock.nickname } returns nickName
    every { mock.note } returns Note(note)

    return mock
}
