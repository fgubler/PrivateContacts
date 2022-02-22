/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository

import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataCategory.EMAIL
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataCategory.PHONE_NUMBER
import ch.abwesend.privatecontacts.domain.model.result.ContactChangeError.UNKNOWN_ERROR
import ch.abwesend.privatecontacts.domain.model.result.ContactDeleteResult
import ch.abwesend.privatecontacts.domain.model.result.ContactSaveResult
import ch.abwesend.privatecontacts.domain.service.FullTextSearchService
import ch.abwesend.privatecontacts.infrastructure.room.contact.toEntity
import ch.abwesend.privatecontacts.testutil.TestBase
import ch.abwesend.privatecontacts.testutil.someContactBase
import ch.abwesend.privatecontacts.testutil.someContactDataEntity
import ch.abwesend.privatecontacts.testutil.someContactEntity
import ch.abwesend.privatecontacts.testutil.someContactFull
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.runs
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.core.module.Module
import java.util.UUID

@ExperimentalCoroutinesApi
@ExtendWith(MockKExtension::class)
class ContactRepositoryTest : TestBase() {
    @MockK
    private lateinit var contactDataRepository: ContactDataRepository

    @RelaxedMockK
    private lateinit var searchService: FullTextSearchService

    @InjectMockKs
    private lateinit var underTest: ContactRepository

    override fun Module.setupKoinModule() {
        single { contactDataRepository }
        single { searchService }
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
    fun `deleting a contact should delete it`() {
        val contact = someContactFull()
        coEvery { contactDao.delete(ofType<UUID>()) } just runs
        coEvery { contactDataRepository.deleteContactData(any()) } just runs

        val result = runBlocking { underTest.deleteContact(contact) }

        coVerify { contactDao.delete(contact.id.uuid) }
        assertThat(result).isEqualTo(ContactDeleteResult.Success)
    }

    @Test
    fun `deleting a contact should also delete the contact data`() {
        val contact = someContactFull()
        coEvery { contactDao.delete(ofType<UUID>()) } just runs
        coEvery { contactDataRepository.deleteContactData(any()) } just runs

        val result = runBlocking { underTest.deleteContact(contact) }

        coVerify { contactDataRepository.deleteContactData(contact) }
        assertThat(result).isEqualTo(ContactDeleteResult.Success)
    }

    @Test
    fun `an exception during deletion should return an Error-Result`() {
        val contact = someContactFull()
        coEvery { contactDao.delete(ofType<UUID>()) } throws RuntimeException("Test")
        coEvery { contactDataRepository.deleteContactData(any()) } just runs

        val result = runBlocking { underTest.deleteContact(contact) }

        assertThat(result).isEqualTo(ContactDeleteResult.Failure(UNKNOWN_ERROR))
    }

    @Test
    fun `resolving a contact should load the contact-data`() {
        val contact = someContactBase()
        val contactData = listOf(
            someContactDataEntity(contactId = contact.id.uuid),
            someContactDataEntity(contactId = contact.id.uuid),
        )
        coEvery { contactDataRepository.loadContactData(any()) } returns contactData
        coEvery { contactDao.findById(any()) } returns someContactEntity()
        every { contactDataRepository.tryResolveContactData(any()) } returns null

        runBlocking { underTest.resolveContact(contact) }

        coVerify { contactDataRepository.loadContactData(contact) }
    }

    @Test
    fun `resolving a contact should also re-load the base-data from the database`() {
        val contact = someContactBase(firstName = "John")
        val contactEntity = someContactEntity(firstName = "Jack")
        coEvery { contactDataRepository.loadContactData(any()) } returns emptyList()
        coEvery { contactDao.findById(any()) } returns contactEntity
        every { contactDataRepository.tryResolveContactData(any()) } returns null

        val result = runBlocking { underTest.resolveContact(contact) }

        assertThat(result.firstName).isEqualTo(contactEntity.firstName)
        assertThat(result.lastName).isEqualTo(contactEntity.lastName)
        assertThat(result.nickname).isEqualTo(contactEntity.nickname)
        assertThat(result.notes).isEqualTo(contactEntity.notes)
        assertThat(result.type).isEqualTo(contactEntity.type)
    }

    @Test
    fun `resolving a contact should try to resolve phone-numbers`() {
        val contact = someContactBase()
        val contactData = listOf(
            someContactDataEntity(contactId = contact.id.uuid, category = PHONE_NUMBER),
            someContactDataEntity(contactId = contact.id.uuid, category = PHONE_NUMBER),
        )
        coEvery { contactDataRepository.loadContactData(any()) } returns contactData
        coEvery { contactDao.findById(any()) } returns someContactEntity()
        every { contactDataRepository.tryResolveContactData(any()) } returns null

        runBlocking { underTest.resolveContact(contact) }

        coVerify { contactDataRepository.loadContactData(contact) }
        contactData.forEach {
            coVerify { contactDataRepository.tryResolveContactData(it) }
        }
    }

    @Test
    fun `resolving a contact should try to resolve email-addresses`() {
        val contact = someContactBase()
        val contactData = listOf(
            someContactDataEntity(contactId = contact.id.uuid, category = EMAIL),
            someContactDataEntity(contactId = contact.id.uuid, category = EMAIL),
        )
        coEvery { contactDataRepository.loadContactData(any()) } returns contactData
        coEvery { contactDao.findById(any()) } returns someContactEntity()
        every { contactDataRepository.tryResolveContactData(any()) } returns null

        runBlocking { underTest.resolveContact(contact) }

        coVerify { contactDataRepository.loadContactData(contact) }
        contactData.forEach {
            coVerify { contactDataRepository.tryResolveContactData(it) }
        }
    }
}
