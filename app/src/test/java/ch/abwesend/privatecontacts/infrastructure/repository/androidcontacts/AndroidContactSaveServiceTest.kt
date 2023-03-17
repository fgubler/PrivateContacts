/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts

import ch.abwesend.privatecontacts.domain.model.ModelStatus
import ch.abwesend.privatecontacts.domain.model.contact.ContactAccount
import ch.abwesend.privatecontacts.domain.model.contact.ContactIdAndroid
import ch.abwesend.privatecontacts.domain.model.contact.IContact
import ch.abwesend.privatecontacts.domain.model.contact.IContactIdExternal
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactData
import ch.abwesend.privatecontacts.domain.model.result.ContactChangeError.UNABLE_TO_DELETE_CONTACT
import ch.abwesend.privatecontacts.domain.model.result.ContactChangeError.UNABLE_TO_SAVE_CONTACT
import ch.abwesend.privatecontacts.domain.model.result.ContactSaveResult
import ch.abwesend.privatecontacts.domain.service.interfaces.IAddressFormattingService
import ch.abwesend.privatecontacts.domain.service.interfaces.TelephoneService
import ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.factory.IAndroidContactMutableFactory
import ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.factory.toContact
import ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.factory.toInternetAccount
import ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.model.IAndroidContactMutable
import ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.repository.AndroidContactLoadRepository
import ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.repository.AndroidContactSaveRepository
import ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.service.AndroidContactChangeService
import ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.service.AndroidContactLoadService
import ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.service.AndroidContactSaveService
import ch.abwesend.privatecontacts.infrastructure.service.addressformatting.AddressFormattingService
import ch.abwesend.privatecontacts.testutil.TestBase
import ch.abwesend.privatecontacts.testutil.androidcontacts.TestAndroidContactMutableFactory
import ch.abwesend.privatecontacts.testutil.databuilders.someAndroidContactMutable
import ch.abwesend.privatecontacts.testutil.databuilders.someContactEditable
import ch.abwesend.privatecontacts.testutil.databuilders.someContactGroup
import ch.abwesend.privatecontacts.testutil.databuilders.someExternalContactId
import ch.abwesend.privatecontacts.testutil.databuilders.someListOfContactData
import ch.abwesend.privatecontacts.testutil.databuilders.someOnlineAccount
import com.alexstyl.contactstore.InternetAccount
import com.alexstyl.contactstore.MutableContactGroup
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
    private var addressFormattingService: IAddressFormattingService = AddressFormattingService()

    @SpyK
    private var changeService = AndroidContactChangeService()

    @SpyK
    private var mutableContactFactory: IAndroidContactMutableFactory = TestAndroidContactMutableFactory()

    @InjectMockKs
    private lateinit var underTest: AndroidContactSaveService

    override fun setupKoinModule(module: Module) {
        super.setupKoinModule(module)
        module.single { loadRepository }
        module.single { saveRepository }
        module.single { loadService }
        module.single { changeService }
        module.single { telephoneService }
        module.single { addressFormattingService }
        module.single { mutableContactFactory }
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
    fun `should update contact successfully`() {
        val contactId = someExternalContactId()
        val originalContact = someContactEditable(id = contactId, firstName = "some first", lastName = "some last")
        val androidContact = someAndroidContactMutable(contactId = contactId.contactNo)
        val changedContact = someContactEditable(
            id = contactId,
            contactData = someListOfContactData(ModelStatus.NEW)
        )
        coEvery { loadRepository.resolveContactRaw(any()) } returns androidContact
        coEvery { loadService.resolveContact(any(), any()) } returns originalContact
        coEvery { loadService.getContactGroups(any()) } returns emptyList()
        coJustRun { saveRepository.updateContact(any()) }
        coJustRun { saveRepository.createContactGroups(any()) }

        val result = runBlocking { underTest.updateContact(contactId, changedContact) }

        val slot = slot<IAndroidContactMutable>()
        coVerify { saveRepository.updateContact(capture(slot)) }
        assertThat(result).isEqualTo(ContactSaveResult.Success)
        assertThat(slot.isCaptured).isTrue
        val capturedContact = slot.captured
        assertThat(capturedContact.contactId).isEqualTo(contactId.contactNo)
        // properties which were changed
        assertThat(capturedContact.firstName).isEqualTo(changedContact.firstName)
        assertThat(capturedContact.lastName).isEqualTo(changedContact.lastName)
        // properties which were not changed
        assertThat(capturedContact.nickname).isEqualTo(androidContact.nickname)
        assertThat(capturedContact.note?.raw).isEqualTo(androidContact.note?.raw)
        // contact-data
        val capturedContactTransformed = capturedContact.toContact(
            groups = emptyList(),
            telephoneService = telephoneService,
            addressFormattingService = addressFormattingService,
            rethrowExceptions = true,
        )
        assertThat(capturedContactTransformed).isNotNull
        assertContactDataEquals(changedContact, capturedContactTransformed!!)
    }

    @Test
    fun `should catch exceptions during update and return an error`() {
        val contactId = someExternalContactId()
        val originalContact = someContactEditable(id = contactId, firstName = "some first", lastName = "some last")
        val changedContact = someContactEditable(id = contactId)
        val androidContact = someAndroidContactMutable(contactId = contactId.contactNo)
        coEvery { loadRepository.resolveContactRaw(any()) } returns androidContact
        coEvery { loadService.resolveContact(any(), any()) } returns originalContact
        coEvery { saveRepository.updateContact(any()) } throws IllegalArgumentException("Test")

        val result = runBlocking { underTest.updateContact(contactId, changedContact) }

        assertThat(result).isEqualTo(ContactSaveResult.Failure(UNABLE_TO_SAVE_CONTACT))
    }

    @Test
    fun `should create contact successfully`() {
        val account = someOnlineAccount()
        val newContact = someContactEditable(
            saveInAccount = account,
            contactData = someListOfContactData(ModelStatus.NEW),
            contactGroups = listOf(
                someContactGroup(name = "group1"),
                someContactGroup(name = "group2"),
            ),
        )
        val expectedInternetAccount = account.toInternetAccount()
        coJustRun { saveRepository.createContact(any(), any()) }
        coEvery { loadService.getContactGroups(any()) } returns emptyList()
        coJustRun { saveRepository.createContactGroups(any()) }

        val result = runBlocking { underTest.createContact(newContact) }

        val contactSlot = slot<IAndroidContactMutable>()
        val accountSlot = slot<InternetAccount>()
        coVerify { saveRepository.createContact(capture(contactSlot), capture(accountSlot)) }
        assertThat(result).isEqualTo(ContactSaveResult.Success)
        assertThat(contactSlot.isCaptured).isTrue
        assertThat(accountSlot.isCaptured).isTrue
        val capturedContact = contactSlot.captured
        val capturedAccount = accountSlot.captured
        assertThat(capturedAccount).isEqualTo(expectedInternetAccount)
        assertThat(capturedContact.contactId).isEqualTo(-1)
        assertThat(capturedContact.firstName).isEqualTo(newContact.firstName)
        assertThat(capturedContact.lastName).isEqualTo(newContact.lastName)
        assertThat(capturedContact.nickname).isEqualTo(newContact.nickname)
        assertThat(capturedContact.note?.raw).isEqualTo(newContact.notes)
        val capturedContactTransformed = capturedContact.toContact(
            groups = emptyList(),
            telephoneService = telephoneService,
            addressFormattingService = addressFormattingService,
            rethrowExceptions = true,
        )
        assertThat(capturedContactTransformed).isNotNull
        assertContactDataEquals(newContact, capturedContactTransformed!!)
    }

    @Test
    fun `should create contact locally if no account is passed`() {
        val newContact = someContactEditable(saveInAccount = ContactAccount.LocalPhoneContacts)
        coJustRun { saveRepository.createContact(any(), any()) }
        coEvery { loadService.getContactGroups(any()) } returns emptyList()

        runBlocking { underTest.createContact(newContact) }

        coVerify { saveRepository.createContact(any(), saveInAccount = null) }
    }

    @Test
    fun `should create contact-groups in the correct account`() {
        val contactGroups = listOf(
            someContactGroup(name = "group1", modelStatus = ModelStatus.NEW),
            someContactGroup(name = "group2", modelStatus = ModelStatus.NEW),
        )
        val account = someOnlineAccount()
        val expectedInternetAccount = account.toInternetAccount()
        coEvery { loadService.getContactGroups(any()) } returns emptyList()
        coJustRun { saveRepository.createContactGroups(any()) }

        val result = runBlocking { underTest.createMissingContactGroups(account, contactGroups) }

        assertThat(result).isEqualTo(ContactSaveResult.Success)
        val passedGroupsSlot = slot<List<MutableContactGroup>>()
        coVerify { saveRepository.createContactGroups(capture(passedGroupsSlot)) }
        assertThat(passedGroupsSlot.isCaptured).isTrue
        val passedGroups = passedGroupsSlot.captured
        assertThat(passedGroups).hasSize(2)
        assertThat(passedGroups[0].title).isEqualTo(contactGroups[0].id.name)
        assertThat(passedGroups[0].account).isEqualTo(expectedInternetAccount)
        assertThat(passedGroups[1].title).isEqualTo(contactGroups[1].id.name)
        assertThat(passedGroups[1].account).isEqualTo(expectedInternetAccount)
    }

    @Test
    fun `should only create missing contact-groups (matching by name and groupNo)`() {
        val newContactGroups = listOf(
            someContactGroup(groupNo = 111, name = "group1", modelStatus = ModelStatus.NEW), // missing
            someContactGroup(groupNo = 222, name = "group2", modelStatus = ModelStatus.NEW), // missing
            someContactGroup(groupNo = 333, name = "group3", modelStatus = ModelStatus.NEW), // matching by groupNo
            someContactGroup(groupNo = 444, name = "group4", modelStatus = ModelStatus.NEW), // matching by name
        )
        val existingContactGroups = listOf(
            someContactGroup(groupNo = 333, name = "group3X"),
            someContactGroup(groupNo = 999, name = "group4"),

        )
        val account = someOnlineAccount()
        coEvery { loadService.getContactGroups(any()) } returns existingContactGroups
        coJustRun { saveRepository.createContactGroups(any()) }

        val result = runBlocking { underTest.createMissingContactGroups(account, newContactGroups) }

        assertThat(result).isEqualTo(ContactSaveResult.Success)
        val passedGroupsSlot = slot<List<MutableContactGroup>>()
        coVerify { saveRepository.createContactGroups(capture(passedGroupsSlot)) }
        assertThat(passedGroupsSlot.isCaptured).isTrue
        val passedGroups = passedGroupsSlot.captured
        assertThat(passedGroups).hasSize(2)
        assertThat(passedGroups[0].title).isEqualTo(newContactGroups[0].id.name)
        assertThat(passedGroups[1].title).isEqualTo(newContactGroups[1].id.name)
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
