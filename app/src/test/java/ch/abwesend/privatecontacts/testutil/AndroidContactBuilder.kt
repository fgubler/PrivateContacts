package ch.abwesend.privatecontacts.testutil

import com.alexstyl.contactstore.Contact
import io.mockk.every
import io.mockk.mockk

fun someAndroidContact(
    contactId: Long = 123123,
    firstName: String = "Luke",
    lastName: String = "Skywalker",
    displayName: String = "$firstName $lastName",
): Contact {
    val mock = mockk<Contact>()

    every { mock.contactId } returns contactId
    every { mock.firstName } returns firstName
    every { mock.lastName } returns lastName
    every { mock.displayName } returns displayName

    return mock
}
