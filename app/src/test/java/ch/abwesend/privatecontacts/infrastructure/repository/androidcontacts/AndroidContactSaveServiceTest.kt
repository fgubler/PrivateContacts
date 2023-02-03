/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts

import ch.abwesend.privatecontacts.domain.model.ModelStatus
import ch.abwesend.privatecontacts.domain.model.contact.ContactIdAndroid
import ch.abwesend.privatecontacts.domain.model.contact.IContact
import ch.abwesend.privatecontacts.domain.model.contact.IContactIdExternal
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactData
import ch.abwesend.privatecontacts.domain.model.result.ContactChangeError.UNABLE_TO_DELETE_CONTACT
import ch.abwesend.privatecontacts.domain.model.result.ContactChangeError.UNABLE_TO_SAVE_CONTACT
import ch.abwesend.privatecontacts.domain.model.result.ContactSaveResult
import ch.abwesend.privatecontacts.domain.repository.IAddressFormattingRepository
import ch.abwesend.privatecontacts.domain.service.interfaces.TelephoneService
import ch.abwesend.privatecontacts.infrastructure.repository.addressformatting.AddressFormattingRepository
import ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.factory.toContact
import ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.repository.AndroidContactLoadRepository
import ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.repository.AndroidContactSaveRepository
import ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.service.AndroidContactChangeService
import ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.service.AndroidContactLoadService
import ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.service.AndroidContactSaveService
import ch.abwesend.privatecontacts.testutil.TestBase
import ch.abwesend.privatecontacts.testutil.databuilders.someContactEditable
import ch.abwesend.privatecontacts.testutil.databuilders.someEmailAddress
import ch.abwesend.privatecontacts.testutil.databuilders.someEventDate
import ch.abwesend.privatecontacts.testutil.databuilders.someExternalContactId
import ch.abwesend.privatecontacts.testutil.databuilders.someMutableAndroidContact
import ch.abwesend.privatecontacts.testutil.databuilders.somePhoneNumber
import ch.abwesend.privatecontacts.testutil.databuilders.somePhysicalAddress
import ch.abwesend.privatecontacts.testutil.databuilders.someRelationship
import ch.abwesend.privatecontacts.testutil.databuilders.someWebsite
import com.alexstyl.contactstore.MutableContact
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.SpyK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.core.module.Module

@ExperimentalCoroutinesApi
@ExtendWith(MockKExtension::class)
class AndroidContactSaveServiceTest : TestBase() {
    @MockK
    private lateinit var loadRepository: AndroidContactLoadRepository

    @MockK
    private lateinit var saveRepository: AndroidContactSaveRepository

    @MockK
    private lateinit var loadService: AndroidContactLoadService

    @MockK
    private lateinit var telephoneService: TelephoneService

    @SpyK
    private var addressFormattingRepository: IAddressFormattingRepository = AddressFormattingRepository()

    @SpyK
    private var changeService = AndroidContactChangeService()

    @InjectMockKs
    private lateinit var underTest: AndroidContactSaveService

    override fun setupKoinModule(module: Module) {
        super.setupKoinModule(module)
        module.single { loadRepository }
        module.single { saveRepository }
        module.single { loadService }
        module.single { changeService }
        module.single { telephoneService }
        module.single { addressFormattingRepository }
    }

    override fun setup() {
        super.setup()
        every { telephoneService.formatPhoneNumberForMatching(any()) } answers { firstArg() }
        every { telephoneService.formatPhoneNumberForDisplay(any()) } answers { firstArg() }
    }

    @Test
    fun `should delete contacts and return success`() {
        val contactIds = listOf(
            ContactIdAndroid(contactNo = 123123),
            ContactIdAndroid(contactNo = 345608),
        )
        coJustRun { saveRepository.deleteContacts(any()) }
        coEvery { loadService.doContactsExist(any()) } answers {
            val passedContactIds = firstArg<Set<IContactIdExternal>>()
            passedContactIds.associateWith { false }
        }

        val result = runBlocking { underTest.deleteContacts(contactIds) }

        coVerify { saveRepository.deleteContacts(contactIds.toSet()) }
        coVerify { loadService.doContactsExist(contactIds.toSet()) }
        assertThat(result.completelySuccessful).isTrue
    }

    @Test
    fun `should delete contacts and return partial success`() {
        val contactIds = listOf(
            ContactIdAndroid(contactNo = 123123),
            ContactIdAndroid(contactNo = 345608),
        )
        coJustRun { saveRepository.deleteContacts(any()) }
        coEvery { loadService.doContactsExist(any()) } answers {
            val passedContactIds = firstArg<Set<IContactIdExternal>>()
            passedContactIds.associateWith { it != passedContactIds.first() }
        }

        val result = runBlocking { underTest.deleteContacts(contactIds) }

        coVerify { saveRepository.deleteContacts(contactIds.toSet()) }
        coVerify { loadService.doContactsExist(contactIds.toSet()) }
        assertThat(result.completelySuccessful).isFalse
        assertThat(result.successfulChanges).isEqualTo(listOf(contactIds[0]))
        assertThat(result.failedChanges.keys).isEqualTo(setOf(contactIds[1]))
        assertThat(result.failedChanges.values.single().errors.single()).isEqualTo(UNABLE_TO_DELETE_CONTACT)
    }

    @Test
    fun `should delete contacts and look for partial success when an exception occurs`() {
        val contactIds = listOf(
            ContactIdAndroid(contactNo = 123123),
            ContactIdAndroid(contactNo = 345608),
        )
        coEvery { saveRepository.deleteContacts(any()) } throws IllegalStateException("Test")
        coEvery { loadService.doContactsExist(any()) } answers {
            val passedContactIds = firstArg<Set<IContactIdExternal>>()
            passedContactIds.associateWith { it != passedContactIds.first() }
        }

        val result = runBlocking { underTest.deleteContacts(contactIds) }

        coVerify { saveRepository.deleteContacts(contactIds.toSet()) }
        coVerify { loadService.doContactsExist(contactIds.toSet()) }
        assertThat(result.completelySuccessful).isFalse
        assertThat(result.successfulChanges).isEqualTo(listOf(contactIds[0]))
        assertThat(result.failedChanges.keys).isEqualTo(setOf(contactIds[1]))
        assertThat(result.failedChanges.values.single().errors.single()).isEqualTo(UNABLE_TO_DELETE_CONTACT)
    }

    @Test
    fun `should update contact base-data successfully`() {
        val contactId = someExternalContactId()
        val originalContact = someContactEditable(id = contactId, firstName = "some first", lastName = "some last")
        val changedContact = someContactEditable(id = contactId)
        val androidContact = someMutableAndroidContact(contactId = contactId.contactNo)
        coEvery { loadRepository.resolveContactRaw(any()) } returns androidContact
        coEvery { loadService.resolveContact(any(), any()) } returns originalContact
        coJustRun { saveRepository.updateContact(any()) }

        val result = runBlocking { underTest.updateContact(contactId, changedContact) }

        val slot = slot<MutableContact>()
        coVerify { saveRepository.updateContact(capture(slot)) }
        assertThat(result).isEqualTo(ContactSaveResult.Success)
        assertThat(slot.isCaptured).isTrue
        val capturedContact = slot.captured
        assertThat(capturedContact.contactId).isEqualTo(contactId.contactNo)
        assertThat(capturedContact.firstName).isEqualTo(changedContact.firstName)
        assertThat(capturedContact.lastName).isEqualTo(changedContact.lastName)
        // TODO check why these two do not work
//        assertThat(capturedContact.nickname).isEqualTo(changedContact.nickname)
//        assertThat(capturedContact.note).isEqualTo(changedContact.notes)
    }

    // TODO fix
//    @Test
//    fun `should update contact contact-data successfully`() {
//        val contactId = someExternalContactId()
//        val originalContact = someContactEditable(id = contactId)
//        val changedContact = someContactEditable(
//            id = contactId,
//            contactData = listOf(
//                somePhoneNumber(modelStatus = ModelStatus.NEW),
//                someEmailAddress(modelStatus = ModelStatus.NEW),
//                somePhysicalAddress(modelStatus = ModelStatus.NEW),
//                someEventDate(modelStatus = ModelStatus.NEW),
//                someWebsite(modelStatus = ModelStatus.NEW),
//                someRelationship(modelStatus = ModelStatus.NEW),
//            )
//        )
//        val androidContact = someMutableAndroidContact(contactId = contactId.contactNo)
//        coEvery { loadRepository.resolveContactRaw(any()) } returns androidContact
//        coEvery { loadService.resolveContact(any(), any()) } returns originalContact
//        coJustRun { saveRepository.updateContact(any()) }
//
//        val result = runBlocking { underTest.updateContact(contactId, changedContact) }
//
//        val slot = slot<MutableContact>()
//        coVerify { saveRepository.updateContact(capture(slot)) }
//        assertThat(result).isEqualTo(ContactSaveResult.Success)
//        assertThat(slot.isCaptured).isTrue
//        val capturedContact = slot.captured
//        assertThat(capturedContact.contactId).isEqualTo(contactId.contactNo)
//        val capturedContactTransformed = capturedContact.toContact(emptyList(), rethrowExceptions = true)
//        assertThat(capturedContactTransformed).isNotNull
//        assertContactDataEquals(originalContact, capturedContactTransformed!!)
//    }

    @Test
    fun `should catch exceptions during update and return an error`() {
        val contactId = someExternalContactId()
        val originalContact = someContactEditable(id = contactId, firstName = "some first", lastName = "some last")
        val changedContact = someContactEditable(id = contactId)
        val androidContact = someMutableAndroidContact(contactId = contactId.contactNo)
        coEvery { loadRepository.resolveContactRaw(any()) } returns androidContact
        coEvery { loadService.resolveContact(any(), any()) } returns originalContact
        coEvery { saveRepository.updateContact(any()) } throws IllegalArgumentException("Test")

        val result = runBlocking { underTest.updateContact(contactId, changedContact) }

        assertThat(result).isEqualTo(ContactSaveResult.Failure(UNABLE_TO_SAVE_CONTACT))
    }

    @Test
    fun `should create contact successfully`() {
        val newContact = someContactEditable(
            contactData = listOf(
                somePhoneNumber(modelStatus = ModelStatus.NEW),
                someEmailAddress(modelStatus = ModelStatus.NEW),
                somePhysicalAddress(modelStatus = ModelStatus.NEW),
                someEventDate(modelStatus = ModelStatus.NEW),
                someWebsite(modelStatus = ModelStatus.NEW),
                someRelationship(modelStatus = ModelStatus.NEW),
            )
        )
        coJustRun { saveRepository.createContact(any(), any()) }

        val result = runBlocking { underTest.createContact(newContact) }

        val contactSlot = slot<MutableContact>()
        coVerify { saveRepository.createContact(capture(contactSlot), any()) }
        assertThat(result).isEqualTo(ContactSaveResult.Success)
        assertThat(contactSlot.isCaptured).isTrue
        val capturedContact = contactSlot.captured
        assertThat(capturedContact.contactId).isEqualTo(-1)
        assertThat(capturedContact.firstName).isEqualTo(newContact.firstName)
        assertThat(capturedContact.lastName).isEqualTo(newContact.lastName)
        // TODO check why these two don't work
//        assertThat(capturedContact.nickname).isEqualTo(newContact.nickname)
//        assertThat(capturedContact.note).isEqualTo(newContact.notes)
        val capturedContactTransformed = capturedContact.toContact(groups = emptyList(), rethrowExceptions = true)
        assertThat(capturedContactTransformed).isNotNull
        // TODO fix
        //        assertContactDataEquals(newContact, capturedContactTransformed!!)
    }

    private fun assertContactDataEquals(contact1: IContact, contact2: IContact) {
        val data1 = contact1.contactDataSet.pseudoSort()
        val data2 = contact2.contactDataSet.pseudoSort()
        assertThat(data2).hasSameSizeAs(data1)
        data2.indices.forEach { index ->
            assertThat(data2[index].category).isEqualTo(data1[index].category)
            assertThat(data2[index].type).isEqualTo(data1[index].type)
            assertThat(data2[index].displayValue).isEqualTo(data1[index].displayValue)
        }
    }

    private fun List<ContactData>.pseudoSort(): List<ContactData> =
        sortedBy { it.category.ordinal * 100 + it.sortOrder }
}
