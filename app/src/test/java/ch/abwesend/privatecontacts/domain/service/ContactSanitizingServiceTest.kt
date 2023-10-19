/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.service

import ch.abwesend.privatecontacts.domain.model.contactdata.ContactData
import ch.abwesend.privatecontacts.domain.model.contactdata.StringBasedContactData
import ch.abwesend.privatecontacts.domain.service.interfaces.TelephoneService
import ch.abwesend.privatecontacts.testutil.TestBase
import ch.abwesend.privatecontacts.testutil.databuilders.someContactEditable
import ch.abwesend.privatecontacts.testutil.databuilders.someEmailAddress
import ch.abwesend.privatecontacts.testutil.databuilders.somePhoneNumber
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.core.module.Module

@ExperimentalCoroutinesApi
@ExtendWith(MockKExtension::class)
class ContactSanitizingServiceTest : TestBase(mockTelephoneService = false) {
    @MockK
    private lateinit var telephoneService: TelephoneService

    private lateinit var underTest: ContactSanitizingService

    private val displayPostfix = "ForDisplay"
    private val matchingPostfix = "forMatching"

    override fun setup() {
        super.setup()
        underTest = ContactSanitizingService()
        every { telephoneService.formatPhoneNumberForDisplay(any()) } answers { firstArg<String>() + displayPostfix }
        every { telephoneService.formatPhoneNumberForMatching(any()) } answers { firstArg<String>() + matchingPostfix }
    }

    override fun setupKoinModule(module: Module) {
        super.setupKoinModule(module)
        module.single { telephoneService }
    }

    @Test
    fun `should format number for display`() {
        val firstNumber = "firstNumber"
        val firstNumberSanitized = firstNumber + displayPostfix
        val secondNumber = "secondNumber"
        val secondNumberSanitized = secondNumber + displayPostfix
        val contactData = mutableListOf<ContactData>(
            somePhoneNumber(value = firstNumber),
            somePhoneNumber(value = secondNumber),
            someEmailAddress(),
            someEmailAddress(),
        )
        val contact = someContactEditable(contactData = contactData)

        underTest.sanitizeContact(contact)

        verify(exactly = 2) { telephoneService.formatPhoneNumberForDisplay(any()) }
        verify(exactly = 1) { telephoneService.formatPhoneNumberForDisplay(firstNumber) }
        verify(exactly = 1) { telephoneService.formatPhoneNumberForDisplay(secondNumber) }
        val firstSanitizedValue = (contact.contactDataSet[0] as StringBasedContactData).formattedValue
        val secondSanitizedValue = (contact.contactDataSet[1] as StringBasedContactData).formattedValue
        assertThat(firstSanitizedValue).isEqualTo(firstNumberSanitized)
        assertThat(secondSanitizedValue).isEqualTo(secondNumberSanitized)
    }

    @Test
    fun `should format number for matching`() {
        val firstNumber = "firstNumber"
        val firstNumberSanitized = firstNumber + matchingPostfix
        val secondNumber = "secondNumber"
        val secondNumberSanitized = secondNumber + matchingPostfix
        val contactData = mutableListOf<ContactData>(
            somePhoneNumber(value = firstNumber),
            somePhoneNumber(value = secondNumber),
            someEmailAddress(),
            someEmailAddress(),
        )
        val contact = someContactEditable(contactData = contactData)

        underTest.sanitizeContact(contact)

        verify(exactly = 2) { telephoneService.formatPhoneNumberForMatching(any()) }
        verify(exactly = 1) { telephoneService.formatPhoneNumberForMatching(firstNumber) }
        verify(exactly = 1) { telephoneService.formatPhoneNumberForMatching(secondNumber) }
        val firstSanitizedValue = (contact.contactDataSet[0] as StringBasedContactData).valueForMatching
        val secondSanitizedValue = (contact.contactDataSet[1] as StringBasedContactData).valueForMatching
        assertThat(firstSanitizedValue).isEqualTo(firstNumberSanitized)
        assertThat(secondSanitizedValue).isEqualTo(secondNumberSanitized)
    }
}
