/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts

import ch.abwesend.privatecontacts.domain.model.contact.IContactIdExternal
import ch.abwesend.privatecontacts.testutil.TestBase
import ch.abwesend.privatecontacts.testutil.databuilders.someAndroidContact
import ch.abwesend.privatecontacts.testutil.databuilders.someAndroidContactGroup
import com.alexstyl.contactstore.Contact
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExperimentalCoroutinesApi
@ExtendWith(MockKExtension::class)
class AndroidContactFactoryTest : TestBase() {
    @Test
    fun `should create a ContactBase`() {
        val contactId = 433L
        val displayName = "Darth Vader"
        val androidContact = someAndroidContact(contactId = contactId, displayName = displayName)

        val result = androidContact.toContactBase(rethrowExceptions = true)

        assertThat(result).isNotNull
        assertThat(result!!.id).isInstanceOf(IContactIdExternal::class.java)
        assertThat((result.id as IContactIdExternal).contactNo).isEqualTo(contactId)
        assertThat(result.displayName).isEqualTo(displayName)
    }

    @Test
    fun `should create a full Contact`() {
        val androidContact = someAndroidContact(
            contactId = 433L,
            firstName = "Gabriel",
            lastName = "De Leon",
            nickName = "Black Lion",
            note = "likes silver",
        )
        val contactGroups = listOf(
            someAndroidContactGroup(title = "Group 1"),
            someAndroidContactGroup(title = "Group 2"),
            someAndroidContactGroup(title = "Group 3"),
        )
        mockkStatic(Contact::getContactData)
        every { androidContact.getContactData() } returns emptyList()

        val result = androidContact.toContact(groups = contactGroups, rethrowExceptions = true)

        assertThat(result).isNotNull
        assertThat(result!!.id).isInstanceOf(IContactIdExternal::class.java)
        assertThat((result.id as IContactIdExternal).contactNo).isEqualTo(androidContact.contactId)
        assertThat(result.firstName).isEqualTo(androidContact.firstName)
        assertThat(result.lastName).isEqualTo(androidContact.lastName)
        assertThat(result.nickname).isEqualTo(androidContact.nickname)
        assertThat(result.notes).isEqualTo(androidContact.note?.raw)
        assertThat(result.contactGroups.map { it.id.name }).isEqualTo(contactGroups.map { it.title })
        verify { androidContact.getContactData() }
    }
}
