/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts

import ch.abwesend.privatecontacts.domain.model.contact.ContactIdAndroid
import ch.abwesend.privatecontacts.domain.repository.IAddressFormattingRepository
import ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.repository.AndroidContactLoadRepository
import ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.service.AndroidContactLoadService
import ch.abwesend.privatecontacts.testutil.TestBase
import ch.abwesend.privatecontacts.testutil.databuilders.someAndroidContact
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.core.module.Module

@ExperimentalCoroutinesApi
@ExtendWith(MockKExtension::class)
class AndroidContactLoadServiceTest : TestBase() {
    @MockK
    private lateinit var contactLoadRepository: AndroidContactLoadRepository

    @MockK
    private lateinit var addressFormattingRepository: IAddressFormattingRepository

    @InjectMockKs
    private lateinit var underTest: AndroidContactLoadService

    override fun setupKoinModule(module: Module) {
        super.setupKoinModule(module)
        module.single { contactLoadRepository }
        module.single { addressFormattingRepository }
    }

    override fun setup() {
        super.setup()

        every {
            addressFormattingRepository.formatAddress(any(), any(), any(), any(), any(), any())
        } answers {
            val arguments: List<String> = args.filterIsInstance<String>()
            arguments.filter { it.isNotEmpty() }.joinToString(" ").trim()
        }
    }

    @Test
    fun `should resolve an existing contact by ID`() {
        val contactId = ContactIdAndroid(contactNo = 123123)
        val contact = someAndroidContact(contactId = contactId.contactNo, relaxed = true)
        coEvery { contactLoadRepository.resolveContactRaw(any()) } returns contact
        coEvery { contactLoadRepository.loadContactGroups(any()) } returns emptyList()

        val result = runBlocking { underTest.resolveContact(contactId) }

        coVerify { contactLoadRepository.resolveContactRaw(contactId) }
        assertThat(result.id).isEqualTo(contactId)
    }

    @Test
    fun `resolving a contact should re-throw if the contact could not be mapped`() {
        val contactId = ContactIdAndroid(contactNo = 123123)
        val exception = IllegalStateException("Just some test exception")
        val contact = someAndroidContact(contactId = contactId.contactNo, relaxed = true)
        coEvery { contactLoadRepository.resolveContactRaw(any()) } returns contact
        coEvery { contactLoadRepository.loadContactGroups(any()) } returns emptyList()
        every { contact.note } throws exception

        val thrownException = assertThrows<IllegalStateException> {
            runBlocking { underTest.resolveContact(contactId) }
        }

        assertThat(thrownException).isEqualTo(exception)
    }
}
