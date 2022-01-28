package ch.abwesend.privatecontacts.domain.model

import ch.abwesend.privatecontacts.domain.model.contact.ContactBase
import ch.abwesend.privatecontacts.domain.model.contact.ContactFull
import ch.abwesend.privatecontacts.domain.model.contact.asFull
import ch.abwesend.privatecontacts.domain.model.contact.createNew
import ch.abwesend.privatecontacts.domain.model.contact.getFullName
import ch.abwesend.privatecontacts.domain.model.contact.toContactFull
import ch.abwesend.privatecontacts.testutil.someContactBase
import ch.abwesend.privatecontacts.testutil.someContactFull
import ch.abwesend.privatecontacts.testutil.someContactNonEditable
import ch.abwesend.privatecontacts.testutil.somePhoneNumber
import io.mockk.junit5.MockKExtension
import io.mockk.mockkStatic
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class ContactExtensionsTest {
    @Test
    fun `should get full name`() {
        val contact = someContactBase()

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
    fun `should not change full contact`() {
        val contact = someContactFull()

        val editable = contact.asFull()

        assertThat(editable === contact).isTrue // reference equality
    }

    @Test
    fun `should create full contact if not already`() {
        val contact = someContactNonEditable()
        mockkStatic(ContactBase::toContactFull)

        contact.asFull()

        verify { contact.toContactFull(any()) }
    }

    @Test
    fun `should create full contact from base contact`() {
        val contact = someContactBase()
        val phoneNumber = somePhoneNumber()

        val result = contact.toContactFull(mutableListOf(phoneNumber))

        assertThat(result.id).isEqualTo(contact.id)
        assertThat(result.firstName).isEqualTo(contact.firstName)
        assertThat(result.lastName).isEqualTo(contact.lastName)
        assertThat(result.nickname).isEqualTo(contact.nickname)
        assertThat(result.type).isEqualTo(contact.type)
        assertThat(result.notes).isEqualTo(contact.notes)

        assertThat(result.contactDataSet).isEqualTo(mutableListOf(phoneNumber))
    }

    @Test
    fun `should create new full contact`() {
        val result = ContactFull.createNew()

        assertThat(result.isNew).isTrue
    }
}
