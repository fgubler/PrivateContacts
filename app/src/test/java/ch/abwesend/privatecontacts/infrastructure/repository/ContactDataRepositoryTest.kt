/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository

import ch.abwesend.privatecontacts.domain.model.ModelStatus.CHANGED
import ch.abwesend.privatecontacts.domain.model.ModelStatus.DELETED
import ch.abwesend.privatecontacts.domain.model.ModelStatus.NEW
import ch.abwesend.privatecontacts.domain.model.contactdata.Company
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataCategory.ADDRESS
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataCategory.COMPANY
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataCategory.EMAIL
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataCategory.PHONE_NUMBER
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataCategory.WEBSITE
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.CustomValue
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.Key.BUSINESS
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.Key.CUSTOM
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.Key.MAIN
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.Key.PERSONAL
import ch.abwesend.privatecontacts.domain.model.contactdata.EmailAddress
import ch.abwesend.privatecontacts.domain.model.contactdata.PhoneNumber
import ch.abwesend.privatecontacts.domain.model.contactdata.PhysicalAddress
import ch.abwesend.privatecontacts.domain.model.contactdata.StringBasedContactDataSimple
import ch.abwesend.privatecontacts.domain.model.contactdata.Website
import ch.abwesend.privatecontacts.infrastructure.room.contactdata.ContactDataEntity
import ch.abwesend.privatecontacts.infrastructure.room.contactdata.ContactDataTypeEntity
import ch.abwesend.privatecontacts.testutil.TestBase
import ch.abwesend.privatecontacts.testutil.someContactDataEntity
import ch.abwesend.privatecontacts.testutil.someContactFull
import ch.abwesend.privatecontacts.testutil.someContactId
import ch.abwesend.privatecontacts.testutil.somePhoneNumber
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.runs
import io.mockk.slot
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExperimentalCoroutinesApi
@ExtendWith(MockKExtension::class)
class ContactDataRepositoryTest : TestBase() {
    private lateinit var underTest: ContactDataRepository

    override fun setup() {
        underTest = ContactDataRepository()
    }

    @Test
    fun `create should insert the data into the database`() {
        val contact = someContactFull()
        coEvery { contactDataDao.insertAll(any()) } returns Unit

        runBlocking { underTest.createContactData(contact) }

        coVerify { contactDataDao.insertAll(any()) }
    }

    @Test
    fun `create should consider phone numbers`() {
        val contact = spyk(someContactFull())
        coEvery { contactDataDao.insertAll(any()) } returns Unit

        runBlocking { underTest.createContactData(contact) }

        verify { contact.contactDataSet }
    }

    @Test
    fun `update should update the data in the database`() {
        val contact = someContactFull()
        coEvery { contactDataDao.updateAll(any()) } just runs
        coEvery { contactDataDao.insertAll(any()) } just runs
        coEvery { contactDataDao.deleteAll(any()) } just runs

        runBlocking { underTest.updateContactData(contact) }

        coVerify { contactDataDao.updateAll(any()) }
    }

    @Test
    fun `delete should delete all data for that contact`() {
        val contact = someContactFull()
        val contactDataEntities = listOf(
            someContactDataEntity(contactId = contact.id.uuid),
            someContactDataEntity(contactId = contact.id.uuid),
        )
        coEvery { contactDataDao.getDataForContacts(any()) } returns contactDataEntities
        coEvery { contactDataDao.deleteAll(any()) } just runs

        runBlocking { underTest.deleteContactData(contact) }

        coVerify { contactDataDao.getDataForContacts(listOf(contact.id.uuid)) }
        coVerify { contactDataDao.deleteAll(contactDataEntities) }
    }

    @Test
    fun `update should consider phone numbers`() {
        val contact = spyk(someContactFull())
        coEvery { contactDataDao.updateAll(any()) } just runs
        coEvery { contactDataDao.insertAll(any()) } just runs
        coEvery { contactDataDao.deleteAll(any()) } just runs

        runBlocking { underTest.updateContactData(contact) }

        verify { contact.contactDataSet }
    }

    @Test
    fun `update should update existing phone numbers `() {
        val existingNumber = somePhoneNumber(value = "5678", modelStatus = CHANGED)
        val contact = spyk(someContactFull(contactData = listOf(existingNumber)))
        coEvery { contactDataDao.updateAll(any()) } just runs
        coEvery { contactDataDao.insertAll(any()) } just runs
        coEvery { contactDataDao.deleteAll(any()) } just runs

        runBlocking { underTest.updateContactData(contact) }

        verify { contact.contactDataSet }
        val updateSlot = slot<List<ContactDataEntity>>()
        coVerify { contactDataDao.updateAll(capture(updateSlot)) }
        coVerify { contactDataDao.insertAll(emptyList()) }
        coVerify { contactDataDao.deleteAll(emptyList()) }
        confirmVerified(contactDataDao)
        assertThat(updateSlot.captured).hasSize(1)
        assertThat(updateSlot.captured.first().id).isEqualTo(existingNumber.id.uuid)
    }

    @Test
    fun `update should insert new phone numbers`() {
        val newNumber = somePhoneNumber(value = "1234", modelStatus = NEW)
        val contact = spyk(someContactFull(contactData = listOf(newNumber)))
        coEvery { contactDataDao.updateAll(any()) } just runs
        coEvery { contactDataDao.insertAll(any()) } just runs
        coEvery { contactDataDao.deleteAll(any()) } just runs

        runBlocking { underTest.updateContactData(contact) }

        verify { contact.contactDataSet }
        val insertSlot = slot<List<ContactDataEntity>>()
        coVerify { contactDataDao.insertAll(capture(insertSlot)) }
        coVerify { contactDataDao.updateAll(emptyList()) }
        coVerify { contactDataDao.deleteAll(emptyList()) }
        assertThat(insertSlot.captured).hasSize(1)
        assertThat(insertSlot.captured.first().id).isEqualTo(newNumber.id.uuid)
    }

    @Test
    fun `update should delete deleted phone numbers`() {
        val deletedNumber = somePhoneNumber(value = "1234", modelStatus = DELETED)
        val contact = spyk(someContactFull(contactData = listOf(deletedNumber)))
        coEvery { contactDataDao.updateAll(any()) } just runs
        coEvery { contactDataDao.insertAll(any()) } just runs
        coEvery { contactDataDao.deleteAll(any()) } just runs

        runBlocking { underTest.updateContactData(contact) }

        verify { contact.contactDataSet }
        val deleteSlot = slot<List<ContactDataEntity>>()
        coVerify { contactDataDao.deleteAll(capture(deleteSlot)) }
        coVerify { contactDataDao.insertAll(emptyList()) }
        coVerify { contactDataDao.updateAll(emptyList()) }
        assertThat(deleteSlot.captured).hasSize(1)
        assertThat(deleteSlot.captured.first().id).isEqualTo(deletedNumber.id.uuid)
    }

    @Test
    fun `load should load the contact-data`() {
        val contact = someContactFull()
        coEvery { contactDataDao.getDataForContact(any()) } returns emptyList()

        runBlocking { underTest.loadContactData(contact) }

        coVerify { contactDataDao.getDataForContact(contact.id.uuid) }
    }

    @Test
    fun `resolving a phone-number should return it for normal types`() {
        val entity = someContactDataEntity(
            category = PHONE_NUMBER,
            type = ContactDataTypeEntity(PERSONAL, null)
        )

        val result = runBlocking { underTest.tryResolveContactData(entity) }

        assertThat(result).isNotNull
        assertThat(result).isInstanceOf(PhoneNumber::class.java)
        assertThat(result!!.id.uuid).isEqualTo(entity.id)
        assertThat(result.category).isEqualTo(entity.category)
        assertThat((result as? StringBasedContactDataSimple)?.value).isEqualTo(entity.valueRaw)
        assertThat(result.sortOrder).isEqualTo(entity.sortOrder)
        assertThat(result.type.key).isEqualTo(entity.type.key)
        assertThat(result.isMain).isEqualTo(entity.isMain)
    }

    @Test
    fun `resolving a phone-number should return it for type custom`() {
        val entity = someContactDataEntity(
            category = PHONE_NUMBER,
            type = ContactDataTypeEntity(CUSTOM, "TestCustomValue")
        )

        val result = runBlocking { underTest.tryResolveContactData(entity) }

        assertThat(result).isNotNull
        assertThat(result).isInstanceOf(PhoneNumber::class.java)
        assertThat(result!!.id.uuid).isEqualTo(entity.id)
        assertThat(result.category).isEqualTo(entity.category)
        assertThat((result as? StringBasedContactDataSimple)?.value).isEqualTo(entity.valueRaw)
        assertThat(result.sortOrder).isEqualTo(entity.sortOrder)
        assertThat(result.type.key).isEqualTo(entity.type.key)
        assertThat(result.type).isInstanceOf(CustomValue::class.java)
        assertThat((result.type as CustomValue).customValue).isEqualTo(entity.type.customValue)
        assertThat(result.isMain).isEqualTo(entity.isMain)
    }

    @Test
    fun `resolving an email-address should return it`() {
        val entity = someContactDataEntity(
            category = EMAIL,
            type = ContactDataTypeEntity(BUSINESS, null)
        )

        val result = runBlocking { underTest.tryResolveContactData(entity) }

        assertThat(result).isNotNull
        assertThat(result).isInstanceOf(EmailAddress::class.java)
        assertThat(result!!.id.uuid).isEqualTo(entity.id)
        assertThat(result.category).isEqualTo(entity.category)
        assertThat((result as? StringBasedContactDataSimple)?.value).isEqualTo(entity.valueRaw)
        assertThat(result.sortOrder).isEqualTo(entity.sortOrder)
        assertThat(result.type.key).isEqualTo(entity.type.key)
        assertThat(result.isMain).isEqualTo(entity.isMain)
    }

    @Test
    fun `resolving an physical-address should return it`() {
        val entity = someContactDataEntity(
            category = ADDRESS,
            type = ContactDataTypeEntity(BUSINESS, null)
        )

        val result = runBlocking { underTest.tryResolveContactData(entity) }

        assertThat(result).isNotNull
        assertThat(result).isInstanceOf(PhysicalAddress::class.java)
        assertThat(result!!.id.uuid).isEqualTo(entity.id)
        assertThat(result.category).isEqualTo(entity.category)
        assertThat((result as? StringBasedContactDataSimple)?.value).isEqualTo(entity.valueRaw)
        assertThat(result.sortOrder).isEqualTo(entity.sortOrder)
        assertThat(result.type.key).isEqualTo(entity.type.key)
        assertThat(result.isMain).isEqualTo(entity.isMain)
    }

    @Test
    fun `resolving an website should return it`() {
        val entity = someContactDataEntity(
            category = WEBSITE,
            type = ContactDataTypeEntity(BUSINESS, null)
        )

        val result = runBlocking { underTest.tryResolveContactData(entity) }

        assertThat(result).isNotNull
        assertThat(result).isInstanceOf(Website::class.java)
        assertThat(result!!.id.uuid).isEqualTo(entity.id)
        assertThat(result.category).isEqualTo(entity.category)
        assertThat((result as? StringBasedContactDataSimple)?.value).isEqualTo(entity.valueRaw)
        assertThat(result.sortOrder).isEqualTo(entity.sortOrder)
        assertThat(result.type.key).isEqualTo(entity.type.key)
        assertThat(result.isMain).isEqualTo(entity.isMain)
    }

    @Test
    fun `resolving an company should return it`() {
        val entity = someContactDataEntity(
            category = COMPANY,
            type = ContactDataTypeEntity(MAIN, null)
        )

        val result = runBlocking { underTest.tryResolveContactData(entity) }

        assertThat(result).isNotNull
        assertThat(result).isInstanceOf(Company::class.java)
        assertThat(result!!.id.uuid).isEqualTo(entity.id)
        assertThat(result.category).isEqualTo(entity.category)
        assertThat((result as? StringBasedContactDataSimple)?.value).isEqualTo(entity.valueRaw)
        assertThat(result.sortOrder).isEqualTo(entity.sortOrder)
        assertThat(result.type.key).isEqualTo(entity.type.key)
        assertThat(result.isMain).isEqualTo(entity.isMain)
    }

    @Test
    fun `should group phone numbers by contactId`() {
        val contactId1 = someContactId()
        val contactId2 = someContactId()
        val phoneNumberEnd = "321"
        val entities = listOf(
            someContactDataEntity(
                contactId = contactId1.uuid,
                category = PHONE_NUMBER,
                type = ContactDataTypeEntity(MAIN, null),
                value = "123321",
            ),
            someContactDataEntity(
                contactId = contactId1.uuid,
                category = PHONE_NUMBER,
                type = ContactDataTypeEntity(MAIN, null),
                value = "444433321",
            ),
            someContactDataEntity(
                contactId = contactId2.uuid,
                category = PHONE_NUMBER,
                type = ContactDataTypeEntity(MAIN, null),
                value = "4444321",
            ),
        )
        coEvery { contactDataDao.findPhoneNumbersEndingOn(any()) } returns entities

        val result = runBlocking { underTest.findPhoneNumbersEndingOn(phoneNumberEnd) }

        assertThat(result).hasSize(2)
        assertThat(result.keys).containsExactly(contactId1, contactId2)
        coVerify { contactDataDao.findPhoneNumbersEndingOn(phoneNumberEnd) }
    }
}
