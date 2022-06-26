/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository

import ch.abwesend.privatecontacts.domain.model.contact.ContactIdInternal
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataCategory.EMAIL
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataCategory.PHONE_NUMBER
import ch.abwesend.privatecontacts.domain.model.contactdata.PhoneNumberValue
import ch.abwesend.privatecontacts.domain.model.result.ContactChangeError.UNKNOWN_ERROR
import ch.abwesend.privatecontacts.domain.model.result.ContactDeleteResult
import ch.abwesend.privatecontacts.domain.model.result.ContactSaveResult
import ch.abwesend.privatecontacts.domain.service.FullTextSearchService
import ch.abwesend.privatecontacts.infrastructure.room.contact.toEntity
import ch.abwesend.privatecontacts.testutil.TestBase
import ch.abwesend.privatecontacts.testutil.someContactDataEntity
import ch.abwesend.privatecontacts.testutil.someContactEditableWithId
import ch.abwesend.privatecontacts.testutil.someContactEntity
import ch.abwesend.privatecontacts.testutil.someContactId
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
        val (contactId, contact) = someContactEditableWithId()
        coEvery { contactDao.insert(any()) } returns Unit
        coEvery { contactDataRepository.createContactData(any(), any()) } returns Unit

        val result = runBlocking { underTest.createContact(contactId, contact) }

        coVerify { contactDao.insert(contact.toEntity(contactId)) }
        assertThat(result).isEqualTo(ContactSaveResult.Success)
    }

    @Test
    fun `creating a contact should also create contact data`() {
        val (contactId, contact) = someContactEditableWithId()
        coEvery { contactDao.insert(any()) } returns Unit
        coEvery { contactDataRepository.createContactData(any(), any()) } returns Unit

        val result = runBlocking { underTest.createContact(contactId, contact) }

        coVerify { contactDataRepository.createContactData(contactId, contact) }
        assertThat(result).isEqualTo(ContactSaveResult.Success)
    }

    @Test
    fun `an exception during creation should return an Error-Result`() {
        val (contactId, contact) = someContactEditableWithId()
        coEvery { contactDao.insert(any()) } throws RuntimeException("Test")

        val result = runBlocking { underTest.createContact(contactId, contact) }

        assertThat(result).isEqualTo(ContactSaveResult.Failure(UNKNOWN_ERROR))
    }

    @Test
    fun `updating a contact should update it in the database`() {
        val (contactId, contact) = someContactEditableWithId()
        coEvery { contactDao.update(any()) } returns Unit
        coEvery { contactDataRepository.updateContactData(any(), any()) } returns Unit

        val result = runBlocking { underTest.updateContact(contactId, contact) }

        coVerify { contactDao.update(contact.toEntity(contactId)) }
        assertThat(result).isEqualTo(ContactSaveResult.Success)
    }

    @Test
    fun `updating a contact should also update the contact data`() {
        val (contactId, contact) = someContactEditableWithId()
        coEvery { contactDao.update(any()) } returns Unit
        coEvery { contactDataRepository.updateContactData(any(), any()) } returns Unit

        val result = runBlocking { underTest.updateContact(contactId, contact) }

        coVerify { contactDataRepository.updateContactData(contactId, contact) }
        assertThat(result).isEqualTo(ContactSaveResult.Success)
    }

    @Test
    fun `an exception during update should return an Error-Result`() {
        val (contactId, contact) = someContactEditableWithId()
        coEvery { contactDao.update(any()) } throws RuntimeException("Test")

        val result = runBlocking { underTest.updateContact(contactId, contact) }

        assertThat(result).isEqualTo(ContactSaveResult.Failure(UNKNOWN_ERROR))
    }

    @Test
    fun `deleting a contact should delete it`() {
        val contactId = someContactId()
        coEvery { contactDao.delete(ofType<Collection<UUID>>()) } just runs

        val result = runBlocking { underTest.deleteContacts(listOf(contactId)) }

        coVerify { contactDao.delete(listOf(contactId.uuid)) }
        assertThat(result).isEqualTo(ContactDeleteResult.Success)
    }

    @Test
    fun `an exception during deletion should return an Error-Result`() {
        val contactId = someContactId()
        coEvery { contactDao.delete(ofType<UUID>()) } throws RuntimeException("Test")

        val result = runBlocking { underTest.deleteContacts(listOf(contactId)) }

        assertThat(result).isEqualTo(ContactDeleteResult.Failure(UNKNOWN_ERROR))
    }

    @Test
    fun `resolving a contact should load the contact-data`() {
        val contactId = someContactId()
        val contactData = listOf(
            someContactDataEntity(contactId = contactId.uuid),
            someContactDataEntity(contactId = contactId.uuid),
        )
        coEvery { contactDataRepository.loadContactData(any()) } returns contactData
        coEvery { contactDao.findById(any()) } returns someContactEntity()
        every { contactDataRepository.tryResolveContactData(any()) } returns null

        runBlocking { underTest.resolveContact(contactId) }

        coVerify { contactDataRepository.loadContactData(contactId) }
    }

    @Test
    fun `resolving a contact should also re-load the base-data from the database`() {
        val contactId = someContactId()
        val contactEntity = someContactEntity(firstName = "Jack")
        coEvery { contactDataRepository.loadContactData(any()) } returns emptyList()
        coEvery { contactDao.findById(any()) } returns contactEntity
        every { contactDataRepository.tryResolveContactData(any()) } returns null

        val result = runBlocking { underTest.resolveContact(contactId) }

        assertThat(result.firstName).isEqualTo(contactEntity.firstName)
        assertThat(result.lastName).isEqualTo(contactEntity.lastName)
        assertThat(result.nickname).isEqualTo(contactEntity.nickname)
        assertThat(result.notes).isEqualTo(contactEntity.notes)
        assertThat(result.type).isEqualTo(contactEntity.type)
    }

    @Test
    fun `resolving a contact should try to resolve phone-numbers`() {
        val contactId = someContactId()
        val contactData = listOf(
            someContactDataEntity(contactId = contactId.uuid, category = PHONE_NUMBER),
            someContactDataEntity(contactId = contactId.uuid, category = PHONE_NUMBER),
        )
        coEvery { contactDataRepository.loadContactData(any()) } returns contactData
        coEvery { contactDao.findById(any()) } returns someContactEntity()
        every { contactDataRepository.tryResolveContactData(any()) } returns null

        runBlocking { underTest.resolveContact(contactId) }

        coVerify { contactDataRepository.loadContactData(contactId) }
        contactData.forEach {
            coVerify { contactDataRepository.tryResolveContactData(it) }
        }
    }

    @Test
    fun `resolving a contact should try to resolve email-addresses`() {
        val contactId = someContactId()
        val contactData = listOf(
            someContactDataEntity(contactId = contactId.uuid, category = EMAIL),
            someContactDataEntity(contactId = contactId.uuid, category = EMAIL),
        )
        coEvery { contactDataRepository.loadContactData(any()) } returns contactData
        coEvery { contactDao.findById(any()) } returns someContactEntity()
        every { contactDataRepository.tryResolveContactData(any()) } returns null

        runBlocking { underTest.resolveContact(contactId) }

        coVerify { contactDataRepository.loadContactData(contactId) }
        contactData.forEach {
            coVerify { contactDataRepository.tryResolveContactData(it) }
        }
    }

    @Test
    fun `should find contacts with matching phone numbers`() {
        val contactId1 = someContactId()
        val contactId2 = someContactId()
        val phoneNumberEnd = "321"
        val contactData = mapOf(
            contactId1 to listOf(
                PhoneNumberValue("123321"),
                PhoneNumberValue("444321"),
            ),
            contactId2 to listOf(
                PhoneNumberValue("666321"),
            )
        )
        coEvery { contactDao.findByIds(any()) } answers {
            firstArg<List<UUID>>().map { someContactEntity(id = ContactIdInternal(it)) }
        }
        coEvery { contactDataRepository.findPhoneNumbersEndingOn(any()) } returns contactData

        val result = runBlocking { underTest.findContactsWithNumberEndingOn(phoneNumberEnd) }

        coVerify { contactDao.findByIds(listOf(contactId1.uuid, contactId2.uuid)) }
        coVerify { contactDataRepository.findPhoneNumbersEndingOn(phoneNumberEnd) }
        assertThat(result).hasSize(2)
        assertThat(result[0].id).isEqualTo(contactId1)
        assertThat(result[0].phoneNumbers).hasSize(2)
        assertThat(result[1].id).isEqualTo(contactId2)
        assertThat(result[1].phoneNumbers).hasSize(1)
    }
}
