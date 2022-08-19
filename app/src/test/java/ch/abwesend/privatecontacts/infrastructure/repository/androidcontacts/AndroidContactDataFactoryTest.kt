/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts

import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType
import ch.abwesend.privatecontacts.domain.model.contactdata.EmailAddress
import ch.abwesend.privatecontacts.domain.model.contactdata.EventDate
import ch.abwesend.privatecontacts.domain.model.contactdata.PhoneNumber
import ch.abwesend.privatecontacts.domain.model.contactdata.PhysicalAddress
import ch.abwesend.privatecontacts.domain.model.contactdata.Relationship
import ch.abwesend.privatecontacts.domain.model.contactdata.Website
import ch.abwesend.privatecontacts.domain.service.interfaces.TelephoneService
import ch.abwesend.privatecontacts.testutil.TestBase
import ch.abwesend.privatecontacts.testutil.databuilders.someAndroidContact
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.core.module.Module
import java.time.LocalDate

@ExperimentalCoroutinesApi
@ExtendWith(MockKExtension::class)
class AndroidContactDataFactoryTest : TestBase() {
    @MockK
    private lateinit var telephoneService: TelephoneService

    override fun setupKoinModule(module: Module) {
        super.setupKoinModule(module)
        module.single { telephoneService }
    }

    override fun setup() {
        super.setup()
        every { telephoneService.formatPhoneNumberForDisplay(any()) } answers { firstArg() }
        every { telephoneService.formatPhoneNumberForMatching(any()) } answers { firstArg() }
    }

    @Test
    fun `should get phone numbers`() {
        val phoneNumbers = listOf("123", "456", "789")
        val androidContact = someAndroidContact(phoneNumbers = phoneNumbers)

        val result = androidContact.getContactData()

        assertThat(result).hasSameSizeAs(phoneNumbers)
        result.forEachIndexed { index, contactData ->
            assertThat(contactData).isInstanceOf(PhoneNumber::class.java)
            assertThat((contactData as PhoneNumber).value).isEqualTo(phoneNumbers[index])
            assertThat(contactData.type).isEqualTo(ContactDataType.Mobile)
        }
    }

    @Test
    fun `should get email addresses`() {
        val emailAddresses = listOf("a@b.ch", "c@d.ch", "e@f.ch")
        val androidContact = someAndroidContact(emails = emailAddresses)

        val result = androidContact.getContactData()

        assertThat(result).hasSameSizeAs(emailAddresses)
        result.forEachIndexed { index, contactData ->
            assertThat(contactData).isInstanceOf(EmailAddress::class.java)
            assertThat((contactData as EmailAddress).value).isEqualTo(emailAddresses[index])
            assertThat(contactData.type).isEqualTo(ContactDataType.Other)
        }
    }

    @Test
    fun `should get websites`() {
        val websites = listOf("www.abc.ch", "https://www.skynet.com", "http://www.old.org")
        val androidContact = someAndroidContact(websites = websites)

        val result = androidContact.getContactData()

        assertThat(result).hasSameSizeAs(websites)
        result.forEachIndexed { index, contactData ->
            assertThat(contactData).isInstanceOf(Website::class.java)
            assertThat((contactData as Website).value).isEqualTo(websites[index])
            assertThat(contactData.type).isEqualTo(ContactDataType.Main)
        }
    }

    @Test
    fun `should get physical addresses`() {
        val addresses = listOf("Alphastreet 15", "Betastreet 16 Baltimore", "Gammastreet 77 Baltimore USA")
        val androidContact = someAndroidContact(addresses = addresses)

        val result = androidContact.getContactData()

        assertThat(result).hasSameSizeAs(addresses)
        result.forEachIndexed { index, contactData ->
            assertThat(contactData).isInstanceOf(PhysicalAddress::class.java)
            assertThat((contactData as PhysicalAddress).value).isEqualTo(addresses[index])
            assertThat(contactData.type).isEqualTo(ContactDataType.Personal)
        }
    }

    @Test
    fun `should get relationships`() {
        val sisters = listOf("Vin", "Nina", "Audrey")
        val androidContact = someAndroidContact(sisters = sisters)

        val result = androidContact.getContactData()

        assertThat(result).hasSameSizeAs(sisters)
        result.forEachIndexed { index, contactData ->
            assertThat(contactData).isInstanceOf(Relationship::class.java)
            assertThat((contactData as Relationship).value).isEqualTo(sisters[index])
            assertThat(contactData.type).isEqualTo(ContactDataType.RelationshipSibling)
        }
    }

    @Test
    fun `should get event-dates`() {
        val now = LocalDate.now()
        val birthdays = listOf(now, now.minusDays(1), now.minusDays(5))
        val androidContact = someAndroidContact(birthdays = birthdays)

        val result = androidContact.getContactData()

        assertThat(result).hasSameSizeAs(birthdays)
        result.forEachIndexed { index, contactData ->
            assertThat(contactData).isInstanceOf(EventDate::class.java)
            assertThat((contactData as EventDate).value).isEqualTo(birthdays[index])
            assertThat(contactData.type).isEqualTo(ContactDataType.Birthday)
        }
    }
}
