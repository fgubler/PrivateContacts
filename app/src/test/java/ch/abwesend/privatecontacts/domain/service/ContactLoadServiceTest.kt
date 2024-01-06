/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.service

import ch.abwesend.privatecontacts.domain.model.contact.ContactId
import ch.abwesend.privatecontacts.domain.model.contact.withAccountInformation
import ch.abwesend.privatecontacts.domain.model.search.ContactSearchConfig
import ch.abwesend.privatecontacts.domain.repository.IAndroidContactLoadService
import ch.abwesend.privatecontacts.domain.repository.IContactRepository
import ch.abwesend.privatecontacts.testutil.TestBase
import ch.abwesend.privatecontacts.testutil.databuilders.someContactBase
import ch.abwesend.privatecontacts.testutil.databuilders.someContactEditable
import ch.abwesend.privatecontacts.testutil.databuilders.someExternalContactId
import ch.abwesend.privatecontacts.testutil.databuilders.someInternalContactId
import ch.abwesend.privatecontacts.testutil.databuilders.someOnlineAccount
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.SpyK
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
    private lateinit var androidContactService: IAndroidContactLoadService

    @MockK
    private lateinit var easterEggService: EasterEggService

    @SpyK
    @InjectMockKs
    private lateinit var underTest: ContactLoadService

    override fun setupKoinModule(module: Module) {
        super.setupKoinModule(module)
        module.single { contactRepository }
        module.single { easterEggService }
        module.single { androidContactService }
    }

    @Test
    fun `search should check for easter eggs`() {
        val query = "Test"
        coEvery { contactRepository.loadContactsAsFlow(any()) } returns mockk()
        every { easterEggService.checkSearchForEasterEggs(any()) } just runs

        runBlocking { underTest.searchSecretContacts(query) }

        verify { easterEggService.checkSearchForEasterEggs(query) }
    }

    @Test
    fun `search should pass the query to the repository`() {
        val query = "Test"
        coEvery { contactRepository.loadContactsAsFlow(any()) } returns mockk()
        every { easterEggService.checkSearchForEasterEggs(any()) } just runs

        runBlocking { underTest.searchSecretContacts(query) }

        verify { easterEggService.checkSearchForEasterEggs(query) }
        coVerify { contactRepository.loadContactsAsFlow(ContactSearchConfig.Query(query)) }
    }

    @Test
    fun `loading all should load internal and external contacts`() {
        coEvery { contactRepository.loadContactsAsFlow(any()) } returns mockk()
        coEvery { androidContactService.loadContactsAsFlow(any()) } returns mockk()

        runBlocking { underTest.loadAllContacts() }

        coVerify { contactRepository.loadContactsAsFlow(ContactSearchConfig.All) }
        coVerify { androidContactService.loadContactsAsFlow(ContactSearchConfig.All) }
    }

    @Test
    fun `search should search for internal and external contacts`() {
        val query = "Test"
        coEvery { contactRepository.loadContactsAsFlow(any()) } returns mockk()
        coEvery { androidContactService.loadContactsAsFlow(any()) } returns mockk()
        every { easterEggService.checkSearchForEasterEggs(any()) } just runs

        runBlocking { underTest.searchAllContacts(query) }

        verify { easterEggService.checkSearchForEasterEggs(query) }
        coVerify { contactRepository.loadContactsAsFlow(ContactSearchConfig.Query(query)) }
        coVerify { androidContactService.loadContactsAsFlow(ContactSearchConfig.Query(query)) }
    }

    @Test
    fun `resolving a contact should resolve an internal contact`() {
        val contactId = someInternalContactId()
        val contact = someContactEditable(id = contactId)
        coEvery { contactRepository.resolveContact(any()) } returns contact

        val result = runBlocking { underTest.resolveContact(contactId) }

        coVerify { contactRepository.resolveContact(contactId) }
        confirmVerified(androidContactService)
        confirmVerified(contactRepository)
        assertThat(result).isEqualTo(contact)
    }

    @Test
    fun `resolving a contact should resolve an external contact`() {
        val contactId = someExternalContactId()
        val contact = someContactEditable(id = contactId)
        coEvery { androidContactService.resolveContact(any()) } returns contact

        val result = runBlocking { underTest.resolveContact(contactId) }

        coVerify { androidContactService.resolveContact(contactId) }
        confirmVerified(androidContactService)
        confirmVerified(contactRepository)
        assertThat(result).isEqualTo(contact)
    }

    @Test
    fun `resolving contacts should resolve internal contacts`() {
        val contactId = someInternalContactId()
        val contact = someContactEditable(id = contactId)
        coEvery { contactRepository.resolveContacts(any()) } returns listOf(contact)

        val result = runBlocking { underTest.resolveContacts(listOf(contactId)) }

        coVerify { contactRepository.resolveContacts(setOf(contactId)) }
        confirmVerified(androidContactService)
        confirmVerified(contactRepository)
        assertThat(result).hasSize(1)
        assertThat(result.first()).isEqualTo(contact)
    }

    @Test
    fun `resolving contacts should resolve external contacts`() {
        val contactId = someExternalContactId()
        val contact = someContactEditable(id = contactId)
        coEvery { androidContactService.resolveContacts(any()) } returns listOf(contact)

        val result = runBlocking { underTest.resolveContacts(listOf(contactId)) }

        coVerify { androidContactService.resolveContacts(setOf(contactId)) }
        confirmVerified(androidContactService)
        confirmVerified(contactRepository)
        assertThat(result).hasSize(1)
        assertThat(result.first()).isEqualTo(contact)
    }

    @Test
    fun `resolving contacts should be able to deal with a mix of internal and external ones`() {
        val internalId = someInternalContactId()
        val externalId = someExternalContactId()
        val contactIds = listOf(internalId, externalId)
        val internalContact = someContactEditable(id = internalId)
        val externalContact = someContactEditable(id = externalId)
        coEvery { contactRepository.resolveContacts(any()) } returns listOf(internalContact)
        coEvery { androidContactService.resolveContacts(any()) } returns listOf(externalContact)

        val result = runBlocking { underTest.resolveContacts(contactIds) }.associateBy { it.id }

        coVerify { contactRepository.resolveContacts(setOf(internalId)) }
        coVerify { androidContactService.resolveContacts(setOf(externalId)) }
        assertThat(result).hasSize(2)
        assertThat(result[internalId]).isEqualTo(internalContact)
        assertThat(result[externalId]).isEqualTo(externalContact)
    }

    @Test
    fun `should keep account information when resolving contacts`() {
        val contacts = listOf(
            someContactBase().withAccountInformation().also { it.saveInAccount = someOnlineAccount("alpha") },
            someContactBase().withAccountInformation().also { it.saveInAccount = someOnlineAccount("beta") },
            someContactBase().withAccountInformation().also { it.saveInAccount = someOnlineAccount("gamma") },
            someContactBase().withAccountInformation().also { it.saveInAccount = someOnlineAccount("delta") },
        )
        coEvery { underTest.resolveContacts(any()) } answers {
            val ids: Collection<ContactId> = firstArg()
            ids.map { someContactEditable(id = it) }
        }
        val expectedAccountsByContactId = contacts.associate { it.id to it.saveInAccount }

        val result = runBlocking { underTest.resolveContactsWithAccountInformation(contacts) }

        assertThat(result).hasSameSizeAs(contacts)
        val accountsByContactId = result.mapValues { (_, contact) -> contact?.saveInAccount }
        assertThat(accountsByContactId).isEqualTo(expectedAccountsByContactId)
    }
}
