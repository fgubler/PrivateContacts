package ch.abwesend.privatecontacts.infrastructure.repository

import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataSubType.CustomValue
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataSubType.Key.CUSTOM
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataSubType.Key.PRIVATE
import ch.abwesend.privatecontacts.infrastructure.room.contactdata.ContactDataSubTypeEntity
import ch.abwesend.privatecontacts.infrastructure.room.contactdata.ContactDataType
import ch.abwesend.privatecontacts.testutil.KoinTestBase
import ch.abwesend.privatecontacts.testutil.someContactDataEntity
import ch.abwesend.privatecontacts.testutil.someContactFull
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.runs
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExperimentalCoroutinesApi
@ExtendWith(MockKExtension::class)
class ContactDataRepositoryTest : KoinTestBase() {
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

        verify { contact.phoneNumbers }
    }

    @Test
    fun `update should update the data in the database`() {
        val contact = someContactFull()
        coEvery { contactDataDao.updateAll(any()) } just runs
        coEvery { contactDataDao.insertAll(any()) } just runs

        runBlocking { underTest.updateContactData(contact) }

        coVerify { contactDataDao.updateAll(any()) }
    }

    @Test
    fun `update should consider phone numbers`() {
        val contact = spyk(someContactFull())
        coEvery { contactDataDao.updateAll(any()) } just runs
        coEvery { contactDataDao.insertAll(any()) } just runs

        runBlocking { underTest.updateContactData(contact) }

        verify { contact.phoneNumbers }
    }

    @Test
    fun `load should load the contact-data`() {
        val contact = someContactFull()
        coEvery { contactDataDao.getDataForContact(any()) } returns emptyList()

        runBlocking { underTest.loadContactData(contact) }

        coVerify { contactDataDao.getDataForContact(contact.id) }
    }

    @Test
    fun `resolving a phone-number should return if the data is of a different type`() {
        val contactData = someContactDataEntity(type = ContactDataType.EMAIL)

        val result = runBlocking { underTest.tryResolvePhoneNumber(contactData) }

        assertThat(result).isNull()
    }

    @Test
    fun `resolving a phone-number should return the phone-number for normal subtypes`() {
        val contactData = someContactDataEntity(
            type = ContactDataType.PHONE_NUMBER,
            subType = ContactDataSubTypeEntity(PRIVATE, null)
        )

        val result = runBlocking { underTest.tryResolvePhoneNumber(contactData) }

        assertThat(result).isNotNull
        assertThat(result!!.id).isEqualTo(contactData.id)
        assertThat(result.value).isEqualTo(contactData.valueRaw)
        assertThat(result.sortOrder).isEqualTo(contactData.sortOrder)
        assertThat(result.type.key).isEqualTo(contactData.subType.key)
        assertThat(result.isMain).isEqualTo(contactData.isMain)
    }

    @Test
    fun `resolving a phone-number should return the phone-number for subtype custom`() {
        val contactData = someContactDataEntity(
            type = ContactDataType.PHONE_NUMBER,
            subType = ContactDataSubTypeEntity(CUSTOM, "TestCustomValue")
        )

        val result = runBlocking { underTest.tryResolvePhoneNumber(contactData) }

        assertThat(result).isNotNull
        assertThat(result!!.id).isEqualTo(contactData.id)
        assertThat(result.value).isEqualTo(contactData.valueRaw)
        assertThat(result.sortOrder).isEqualTo(contactData.sortOrder)
        assertThat(result.type.key).isEqualTo(contactData.subType.key)
        assertThat(result.type).isInstanceOf(CustomValue::class.java)
        assertThat((result.type as CustomValue).customValue).isEqualTo(contactData.subType.customValue)
        assertThat(result.isMain).isEqualTo(contactData.isMain)
    }
}
