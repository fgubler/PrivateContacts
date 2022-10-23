/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model

import ch.abwesend.privatecontacts.domain.model.contact.ContactEditable
import ch.abwesend.privatecontacts.domain.model.contact.IContact
import ch.abwesend.privatecontacts.domain.model.contact.asEditable
import ch.abwesend.privatecontacts.domain.model.contact.getFullName
import ch.abwesend.privatecontacts.domain.model.contact.toContactEditable
import ch.abwesend.privatecontacts.testutil.TestBase
import ch.abwesend.privatecontacts.testutil.databuilders.someContactEditable
import ch.abwesend.privatecontacts.testutil.databuilders.somePhoneNumber
import ch.abwesend.privatecontacts.testutil.databuilders.someTestContact
import io.mockk.junit5.MockKExtension
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExperimentalCoroutinesApi
@ExtendWith(MockKExtension::class)
class ContactExtensionsTest : TestBase() {
    @Test
    fun `should get display name`() {
        val firstName = "John"
        val lastName = "Snow"
        val nickname = ""
        val fullNameStartingWithFirst = getFullName(
            firstName = firstName,
            lastName = lastName,
            nickname = nickname,
            firstNameFirst = true,
        )
        val fullNameStartingWithLast = getFullName(
            firstName = firstName,
            lastName = lastName,
            nickname = nickname,
            firstNameFirst = false,
        )

        assertThat(fullNameStartingWithFirst).isEqualTo("John Snow")
        assertThat(fullNameStartingWithLast).isEqualTo("Snow John")
    }

    @Test
    fun `should get display name with nickname`() {
        val firstName = "John"
        val lastName = "Snow"
        val nickname = "Fix-All"
        val fullNameStartingWithFirst = getFullName(
            firstName = firstName,
            lastName = lastName,
            nickname = nickname,
            firstNameFirst = true,
        )
        val fullNameStartingWithLast = getFullName(
            firstName = firstName,
            lastName = lastName,
            nickname = nickname,
            firstNameFirst = false,
        )

        assertThat(fullNameStartingWithFirst).isEqualTo("""John "Fix-All" Snow""")
        assertThat(fullNameStartingWithLast).isEqualTo("""Snow "Fix-All" John""")
    }

    @Test
    fun `should get display name from contact`() {
        val contact = someContactEditable()
        val fullNameStartingWithFirst = contact.getFullName(firstNameFirst = true)
        val fullNameStartingWithLast = contact.getFullName(firstNameFirst = false)

        assertThat(fullNameStartingWithFirst)
            .startsWith(contact.firstName)
            .endsWith(contact.lastName)
        assertThat(fullNameStartingWithLast)
            .startsWith(contact.lastName)
            .endsWith(contact.firstName)
    }

    @Test
    fun `should reuse editable contact`() {
        val contact = someContactEditable()

        val editable = contact.asEditable()

        assertThat(editable === contact).isTrue // reference equality
    }

    @Test
    fun `should create editable contact if not already`() {
        val contact = someTestContact()
        mockkStatic(IContact::toContactEditable)

        contact.asEditable()

        verify { contact.toContactEditable() }
    }

    @Test
    fun `should create editable contact from base contact`() {
        val phoneNumber = somePhoneNumber()
        val contact = someTestContact(contactData = listOf(phoneNumber))

        val result = contact.toContactEditable()

        assertThat(result.id).isEqualTo(contact.id)
        assertThat(result.firstName).isEqualTo(contact.firstName)
        assertThat(result.lastName).isEqualTo(contact.lastName)
        assertThat(result.nickname).isEqualTo(contact.nickname)
        assertThat(result.type).isEqualTo(contact.type)
        assertThat(result.notes).isEqualTo(contact.notes)
        assertThat(result.contactDataSet).isEqualTo(mutableListOf(phoneNumber))
    }

    @Test
    fun `should create new editable contact`() {
        val result = ContactEditable.createNew()

        assertThat(result.isNew).isTrue
    }
}
