/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.service

import ch.abwesend.privatecontacts.domain.model.search.ContactSearchConfig
import ch.abwesend.privatecontacts.domain.repository.IAndroidContactLoadRepository
import ch.abwesend.privatecontacts.domain.repository.IContactRepository
import ch.abwesend.privatecontacts.testutil.TestBase
import ch.abwesend.privatecontacts.testutil.databuilders.someContactEditable
import ch.abwesend.privatecontacts.testutil.databuilders.someExternalContactId
import ch.abwesend.privatecontacts.testutil.databuilders.someInternalContactId
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.core.module.Module

@ExperimentalCoroutinesApi
@ExtendWith(MockKExtension::class)
class ContactLoadServiceTest : TestBase() {
    @MockK
    private lateinit var contactRepository: IContactRepository

    @MockK
    private lateinit var androidContactRepository: IAndroidContactLoadRepository

    @MockK
    private lateinit var easterEggService: EasterEggService

    @InjectMockKs
    private lateinit var underTest: ContactLoadService

    override fun setupKoinModule(module: Module) {
        super.setupKoinModule(module)
        module.single { contactRepository }
        module.single { easterEggService }
        module.single { androidContactRepository }
    }

    @Test
    fun `search should check for easter eggs`() {
        val query = "Test"
        coEvery { contactRepository.getContactsAsFlow(any()) } returns mockk()
        every { easterEggService.checkSearchForEasterEggs(any()) } just runs

        runBlocking { underTest.searchSecretContacts(query) }

        verify { easterEggService.checkSearchForEasterEggs(query) }
    }

    @Test
    fun `search should pass the query to the paging logic`() {
        val query = "Test"
        coEvery { contactRepository.getContactsAsFlow(any()) } returns mockk()
        every { easterEggService.checkSearchForEasterEggs(any()) } just runs

        runBlocking { underTest.searchSecretContacts(query) }

        verify { easterEggService.checkSearchForEasterEggs(query) }
        coVerify { contactRepository.getContactsAsFlow(ContactSearchConfig.Query(query)) }
    }

    @Test
    fun `resolving a contact should resolve an internal contact`() {
        val contactId = someInternalContactId()
        val contact = someContactEditable(id = contactId)
        coEvery { contactRepository.resolveContact(any()) } returns contact

        val result = runBlocking { underTest.resolveContact(contactId) }

        coVerify { contactRepository.resolveContact(contactId) }
        assertThat(result).isEqualTo(contact)
    }

    @Test
    fun `resolving a contact should resolve an external contact`() {
        val contactId = someExternalContactId()
        val contact = someContactEditable(id = contactId)
        coEvery { androidContactRepository.resolveContact(any()) } returns contact

        val result = runBlocking { underTest.resolveContact(contactId) }

        coVerify { androidContactRepository.resolveContact(contactId) }
        assertThat(result).isEqualTo(contact)
    }

    @Test
    fun `resolving contacts should resolve internal contacts`() {
        val contactId = someInternalContactId()
        val contact = someContactEditable(id = contactId)
        coEvery { contactRepository.resolveContact(any()) } returns contact

        val result = runBlocking { underTest.resolveContacts(listOf(contactId)) }

        coVerify { contactRepository.resolveContact(contactId) }
        assertThat(result).hasSize(1)
        assertThat(result.first()).isEqualTo(contact)
    }

    @Test
    fun `resolving contacts should resolve external contacts`() {
        val contactId = someExternalContactId()
        val contact = someContactEditable(id = contactId)
        coEvery { androidContactRepository.resolveContact(any()) } returns contact

        val result = runBlocking { underTest.resolveContacts(listOf(contactId)) }

        coVerify { androidContactRepository.resolveContact(contactId) }
        assertThat(result).hasSize(1)
        assertThat(result.first()).isEqualTo(contact)
    }
}
