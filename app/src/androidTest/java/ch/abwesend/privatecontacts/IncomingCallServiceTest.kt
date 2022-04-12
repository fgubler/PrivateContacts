/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts

import ch.abwesend.privatecontacts.domain.repository.IContactRepository
import ch.abwesend.privatecontacts.domain.service.IncomingCallService
import ch.abwesend.privatecontacts.testutil.TestBase
import ch.abwesend.privatecontacts.testutil.someContactWithPhoneNumbers
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.core.module.Module

@ExperimentalCoroutinesApi
@ExtendWith(MockKExtension::class)
@Disabled
class IncomingCallServiceTest : TestBase() {
    @MockK
    private lateinit var contactRepository: IContactRepository

    private lateinit var underTest: IncomingCallService

    private val defaultCountryIso = "ch"

    override fun setup() {
        underTest = IncomingCallService()
    }

    override fun Module.setupKoinModule() {
        single { contactRepository }
    }

    @Test
    fun shouldLoadMatchingContacts() {
        val phoneNumber = "123123123"
        coEvery { contactRepository.findContactsWithNumberEndingOn(any()) } returns emptyList()

        runBlocking { underTest.findCorrespondingContacts(phoneNumber, defaultCountryIso) }

        val slot = slot<String>()
        coVerify { contactRepository.findContactsWithNumberEndingOn(capture(slot)) }
        assertThat(phoneNumber).endsWith(slot.captured)
    }

    @Test
    fun shouldFilterForMatchingNumbers() {
        val phoneNumber = "044 123 45 67"
        val matchingContacts = listOf(
            someContactWithPhoneNumbers(phoneNumbers = listOf("0441234567", "123123")),
            someContactWithPhoneNumbers(phoneNumbers = listOf("044 123 45 67")),
            someContactWithPhoneNumbers(phoneNumbers = listOf("+41 44 123 45 67")),
            someContactWithPhoneNumbers(phoneNumbers = listOf("+41441234567")),
            someContactWithPhoneNumbers(phoneNumbers = listOf("044-123-45-67")),
        )
        val notMatchingContacts = listOf(
            someContactWithPhoneNumbers(phoneNumbers = listOf("0441234568", "123123")),
            someContactWithPhoneNumbers(phoneNumbers = listOf("044 123 44 67")),
            someContactWithPhoneNumbers(phoneNumbers = listOf("+41 44 125 45 67")),
            someContactWithPhoneNumbers(phoneNumbers = listOf("+42441234537")),
            someContactWithPhoneNumbers(phoneNumbers = listOf("+41431234567")),
        )
        val allContacts = matchingContacts + notMatchingContacts
        coEvery { contactRepository.findContactsWithNumberEndingOn(any()) } returns allContacts

        val result = runBlocking { underTest.findCorrespondingContacts(phoneNumber, defaultCountryIso) }

        assertThat(result).hasSameSizeAs(matchingContacts)
        assertThat(result).isEqualTo(matchingContacts)
    }
}
