/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts

import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.Business
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.Mobile
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.Other
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.Personal
import ch.abwesend.privatecontacts.domain.model.contactdata.EmailAddress
import ch.abwesend.privatecontacts.domain.model.contactdata.EventDate
import ch.abwesend.privatecontacts.domain.model.contactdata.PhoneNumber
import ch.abwesend.privatecontacts.domain.model.contactdata.PhysicalAddress
import ch.abwesend.privatecontacts.domain.model.contactdata.Relationship
import ch.abwesend.privatecontacts.domain.model.contactdata.Website
import ch.abwesend.privatecontacts.domain.repository.IAddressFormattingRepository
import ch.abwesend.privatecontacts.domain.service.interfaces.TelephoneService
import ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.factory.getContactData
import ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.factory.removeDuplicates
import ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.factory.removePhoneNumberDuplicates
import ch.abwesend.privatecontacts.testutil.TestBase
import ch.abwesend.privatecontacts.testutil.databuilders.someAndroidContact
import ch.abwesend.privatecontacts.testutil.databuilders.somePhoneNumber
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

    @MockK
    private lateinit var addressFormattingRepository: IAddressFormattingRepository

    override fun setupKoinModule(module: Module) {
        super.setupKoinModule(module)
        module.single { telephoneService }
        module.single { addressFormattingRepository }
    }

    override fun setup() {
        super.setup()
        every { telephoneService.formatPhoneNumberForDisplay(any()) } answers { firstArg() }
        every { telephoneService.formatPhoneNumberForMatching(any()) } answers { firstArg() }
        every {
            addressFormattingRepository.formatAddress(any(), any(), any(), any(), any(), any())
        } answers {
            val arguments: List<String> = args.filterIsInstance<String>()
            arguments.filter { it.isNotEmpty() }.joinToString(" ").trim()
        }
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
            assertThat(contactData.type).isEqualTo(Mobile)
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
            assertThat(contactData.type).isEqualTo(Other)
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
            assertThat(contactData.type).isEqualTo(Personal)
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
            assertThat(contactData.type).isEqualTo(ContactDataType.RelationshipBrother)
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

    @Test
    fun `should remove duplicates`() {
        val number1 = "1234"
        val number2 = "12345"
        val number3 = "123456"
        val number4 = "1234567"
        val phoneNumbers = listOf(
            somePhoneNumber(value = number1, type = Mobile),
            somePhoneNumber(value = number2, type = Mobile),
            somePhoneNumber(value = number2, type = Personal),
            somePhoneNumber(value = number3, type = Mobile),
            somePhoneNumber(value = number3, type = Personal),
            somePhoneNumber(value = number3, type = Business),
            somePhoneNumber(value = number4, type = Mobile),
            somePhoneNumber(value = number4, type = Personal),
            somePhoneNumber(value = number4, type = Business),
            somePhoneNumber(value = number4, type = Other),
        )
        val expectedResult = listOf(
            phoneNumbers[0],
            phoneNumbers[1],
            phoneNumbers[3],
            phoneNumbers[6],
        )

        val result = phoneNumbers.removeDuplicates()

        assertThat(result).hasSameSizeAs(expectedResult)
        assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `should remove duplicates of the same number in different formatting`() {
        val number1 = "+41 44 123 44 55"
        val number2 = "+41-44-123-44-55"
        val number3 = "+41441234455"
        val phoneNumbers = listOf(
            somePhoneNumber(value = number1, formattedValue = number2, type = Mobile),
            somePhoneNumber(value = number2, formattedValue = number3, type = Business),
            somePhoneNumber(value = number3, formattedValue = number1, type = Other),
        )
        val expectedResult = listOf(
            phoneNumbers[0],
        )

        val result = phoneNumbers.removePhoneNumberDuplicates()

        assertThat(result).hasSameSizeAs(expectedResult)
        assertThat(result).isEqualTo(expectedResult)
    }
}
