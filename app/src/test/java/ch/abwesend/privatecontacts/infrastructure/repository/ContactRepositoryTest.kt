package ch.abwesend.privatecontacts.infrastructure.repository

import ch.abwesend.privatecontacts.domain.model.result.ContactSaveResult
import ch.abwesend.privatecontacts.domain.model.result.ContactSavingError.UNKNOWN_ERROR
import ch.abwesend.privatecontacts.infrastructure.room.contact.toEntity
import ch.abwesend.privatecontacts.testutil.TestBase
import ch.abwesend.privatecontacts.testutil.someContactBase
import ch.abwesend.privatecontacts.testutil.someContactDataEntity
import ch.abwesend.privatecontacts.testutil.someContactFull
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.core.module.Module

@ExperimentalCoroutinesApi
@ExtendWith(MockKExtension::class)
class ContactRepositoryTest : TestBase() {
    @MockK
    private lateinit var contactDataRepository: ContactDataRepository

    private lateinit var underTest: ContactRepository

    override fun setup() {
        underTest = ContactRepository()
    }

    override fun Module.setupKoinModule() {
        single { contactDataRepository }
    }

    @Test
    fun `creating a contact should insert it into the database`() {
        val contact = someContactFull()
        coEvery { contactDao.insert(any()) } returns Unit
        coEvery { contactDataRepository.createContactData(any()) } returns Unit

        val result = runBlocking { underTest.createContact(contact) }

        coVerify { contactDao.insert(contact.toEntity()) }
        assertThat(result).isEqualTo(ContactSaveResult.Success)
    }

    @Test
    fun `creating a contact should also create contact data`() {
        val contact = someContactFull()
        coEvery { contactDao.insert(any()) } returns Unit
        coEvery { contactDataRepository.createContactData(any()) } returns Unit

        val result = runBlocking { underTest.createContact(contact) }

        coVerify { contactDataRepository.createContactData(contact) }
        assertThat(result).isEqualTo(ContactSaveResult.Success)
    }

    @Test
    fun `an exception during creation should return an Error-Result`() {
        val contact = someContactFull()
        coEvery { contactDao.insert(any()) } throws RuntimeException("Test")

        val result = runBlocking { underTest.createContact(contact) }

        assertThat(result).isEqualTo(ContactSaveResult.Failure(UNKNOWN_ERROR))
    }

    @Test
    fun `updating a contact should update it in the database`() {
        val contact = someContactFull()
        coEvery { contactDao.update(any()) } returns Unit
        coEvery { contactDataRepository.updateContactData(any()) } returns Unit

        val result = runBlocking { underTest.updateContact(contact) }

        coVerify { contactDao.update(contact.toEntity()) }
        assertThat(result).isEqualTo(ContactSaveResult.Success)
    }

    @Test
    fun `updating a contact should also update the contact data`() {
        val contact = someContactFull()
        coEvery { contactDao.update(any()) } returns Unit
        coEvery { contactDataRepository.updateContactData(any()) } returns Unit

        val result = runBlocking { underTest.updateContact(contact) }

        coVerify { contactDataRepository.updateContactData(contact) }
        assertThat(result).isEqualTo(ContactSaveResult.Success)
    }

    @Test
    fun `an exception during update should return an Error-Result`() {
        val contact = someContactFull()
        coEvery { contactDao.update(any()) } throws RuntimeException("Test")

        val result = runBlocking { underTest.updateContact(contact) }

        assertThat(result).isEqualTo(ContactSaveResult.Failure(UNKNOWN_ERROR))
    }

    @Test
    fun `resolving a contact should load the contact-data`() {
        val contact = someContactBase()
        val contactData = listOf(
            someContactDataEntity(contactId = contact.id.uuid),
            someContactDataEntity(contactId = contact.id.uuid),
        )
        coEvery { contactDataRepository.loadContactData(any()) } returns contactData
        every { contactDataRepository.tryResolveContactData(any()) } returns null

        runBlocking { underTest.resolveContact(contact) }

        coVerify { contactDataRepository.loadContactData(contact) }
    }

    @Test
    fun `resolving a contact should use the base-data from the base-contact`() {
        val contact = someContactBase()
        coEvery { contactDataRepository.loadContactData(any()) } returns emptyList()
        every { contactDataRepository.tryResolveContactData(any()) } returns null

        val result = runBlocking { underTest.resolveContact(contact) }

        assertThat(result.firstName).isEqualTo(contact.firstName)
        assertThat(result.lastName).isEqualTo(contact.lastName)
        assertThat(result.nickname).isEqualTo(contact.nickname)
        assertThat(result.notes).isEqualTo(contact.notes)
        assertThat(result.type).isEqualTo(contact.type)
    }

    @Test
    fun `resolving a contact should try to resolve phone-numbers`() {
        val contact = someContactBase()
        val contactData = listOf(
            someContactDataEntity(contactId = contact.id.uuid),
            someContactDataEntity(contactId = contact.id.uuid),
        )
        coEvery { contactDataRepository.loadContactData(any()) } returns contactData
        every { contactDataRepository.tryResolveContactData(any()) } returns null

        runBlocking { underTest.resolveContact(contact) }

        coVerify { contactDataRepository.loadContactData(contact) }
        contactData.forEach {
            coVerify { contactDataRepository.tryResolveContactData(it) }
        }
    }
}
