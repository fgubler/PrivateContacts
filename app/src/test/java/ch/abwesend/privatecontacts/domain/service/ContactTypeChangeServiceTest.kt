/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.service

import ch.abwesend.privatecontacts.domain.model.ModelStatus.CHANGED
import ch.abwesend.privatecontacts.domain.model.ModelStatus.DELETED
import ch.abwesend.privatecontacts.domain.model.ModelStatus.NEW
import ch.abwesend.privatecontacts.domain.model.ModelStatus.UNCHANGED
import ch.abwesend.privatecontacts.domain.model.contact.ContactId
import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.model.contact.ContactType.PUBLIC
import ch.abwesend.privatecontacts.domain.model.contact.ContactType.SECRET
import ch.abwesend.privatecontacts.domain.model.contact.IContactBase
import ch.abwesend.privatecontacts.domain.model.contact.IContactEditable
import ch.abwesend.privatecontacts.domain.model.contact.IContactIdExternal
import ch.abwesend.privatecontacts.domain.model.contact.IContactIdInternal
import ch.abwesend.privatecontacts.domain.model.contact.isExternal
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactData
import ch.abwesend.privatecontacts.domain.model.contactdata.IContactDataIdExternal
import ch.abwesend.privatecontacts.domain.model.contactdata.IContactDataIdInternal
import ch.abwesend.privatecontacts.domain.model.result.ContactChangeError.UNABLE_TO_CREATE_CONTACT_WITH_NEW_TYPE
import ch.abwesend.privatecontacts.domain.model.result.ContactChangeError.UNABLE_TO_DELETE_CONTACT_WITH_OLD_TYPE
import ch.abwesend.privatecontacts.domain.model.result.ContactChangeError.UNKNOWN_ERROR
import ch.abwesend.privatecontacts.domain.model.result.ContactDeleteResult
import ch.abwesend.privatecontacts.domain.model.result.ContactSaveResult.Failure
import ch.abwesend.privatecontacts.domain.model.result.ContactSaveResult.Success
import ch.abwesend.privatecontacts.domain.repository.IContactGroupRepository
import ch.abwesend.privatecontacts.testutil.TestBase
import ch.abwesend.privatecontacts.testutil.databuilders.someContactEditable
import ch.abwesend.privatecontacts.testutil.databuilders.someContactEditableByIdType
import ch.abwesend.privatecontacts.testutil.databuilders.someContactId
import ch.abwesend.privatecontacts.testutil.databuilders.someContactImage
import ch.abwesend.privatecontacts.testutil.databuilders.someEmailAddress
import ch.abwesend.privatecontacts.testutil.databuilders.someExternalContactId
import ch.abwesend.privatecontacts.testutil.databuilders.someInternalContactId
import ch.abwesend.privatecontacts.testutil.databuilders.someListOfContactData
import ch.abwesend.privatecontacts.testutil.databuilders.someListOfExternalContactData
import ch.abwesend.privatecontacts.testutil.databuilders.somePhoneNumber
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.koin.core.module.Module

@ExperimentalCoroutinesApi
@ExtendWith(MockKExtension::class)
class ContactTypeChangeServiceTest : TestBase() {
    @MockK
    private lateinit var saveService: ContactSaveService

    @MockK
    private lateinit var loadService: ContactLoadService

    @MockK
    private lateinit var contactGroupRepository: IContactGroupRepository

    private lateinit var underTest: ContactTypeChangeService

    override fun setup() {
        super.setup()
        underTest = ContactTypeChangeService()

        coJustRun { contactGroupRepository.createMissingContactGroups(any()) }
    }

    override fun setupKoinModule(module: Module) {
        super.setupKoinModule(module)
        module.single { saveService }
        module.single { loadService }
        module.single { contactGroupRepository }
    }

    @Test
    fun `should just save if the type has not changed`() {
        val type = PUBLIC
        val contact = someContactEditable(type = type)
        coEvery { saveService.saveContact(any()) } returns Success

        val result = runBlocking { underTest.changeContactType(contact, type) }

        coVerify { saveService.saveContact(contact) }
        confirmVerified(saveService)
        assertThat(result).isEqualTo(Success)
    }

    @ParameterizedTest
    @MethodSource("getTargetContactTypes")
    @Disabled("Changed the logic to change the type of the ID later")
    fun `should change the ID to ContactIdInternal`(newType: ContactType) {
        val oldId = getContactIdForOtherType(newType)
        val contact = someContactEditableByIdType(id = oldId)
        assertThat(contact.isExternal).isTrue
        coEvery { saveService.saveContact(any()) } returns Success
        coEvery { saveService.deleteContact(any()) } returns ContactDeleteResult.Success

        val result = runBlocking { underTest.changeContactType(contact, newType) }

        assertThat(result).isEqualTo(Success)
        coVerify { saveService.saveContact(match { !it.isExternal }) }
    }

    @ParameterizedTest
    @MethodSource("getTargetContactTypes")
    fun `should change the type to the new type`(newType: ContactType) {
        val oldId = getContactIdForOtherType(newType)
        val contact = someContactEditableByIdType(id = oldId)
        coEvery { saveService.saveContact(any()) } returns Success
        coEvery { saveService.deleteContact(any()) } returns ContactDeleteResult.Success

        val result = runBlocking { underTest.changeContactType(contact, newType) }

        assertThat(result).isEqualTo(Success)
        coVerify { saveService.saveContact(match { it.type == newType }) }
    }

    @ParameterizedTest
    @MethodSource("getTargetContactTypes")
    fun `should change the contact-image from UNCHANGED to NEW`(newType: ContactType) {
        val oldId = getContactIdForOtherType(newType)
        val contact = someContactEditableByIdType(id = oldId, image = someContactImage(modelStatus = UNCHANGED))
        coEvery { saveService.saveContact(any()) } returns Success
        coEvery { saveService.deleteContact(any()) } returns ContactDeleteResult.Success

        val result = runBlocking { underTest.changeContactType(contact, newType) }

        assertThat(result).isEqualTo(Success)
        coVerify { saveService.saveContact(match { it.image.modelStatus == NEW }) }
    }

    @ParameterizedTest
    @MethodSource("getTargetContactTypes")
    fun `should change the contact-image from DELETED to UNCHANGED`(newType: ContactType) {
        val oldId = getContactIdForOtherType(newType)
        val contact = someContactEditableByIdType(id = oldId, image = someContactImage(modelStatus = DELETED))
        coEvery { saveService.saveContact(any()) } returns Success
        coEvery { saveService.deleteContact(any()) } returns ContactDeleteResult.Success

        val result = runBlocking { underTest.changeContactType(contact, newType) }

        assertThat(result).isEqualTo(Success)
        coVerify { saveService.saveContact(match { it.image.modelStatus == UNCHANGED }) }
    }

    @ParameterizedTest
    @MethodSource("getTargetContactTypes")
    fun `should change the contact-data from UNCHANGED and CHANGED to NEW`(newType: ContactType) {
        val oldId = getContactIdForOtherType(newType)
        val contactData = listOf(somePhoneNumber(modelStatus = UNCHANGED), someEmailAddress(modelStatus = CHANGED))
        val contact = someContactEditableByIdType(id = oldId, contactData = contactData)
        coEvery { saveService.saveContact(any()) } returns Success
        coEvery { saveService.deleteContact(any()) } returns ContactDeleteResult.Success

        val result = runBlocking { underTest.changeContactType(contact, newType) }

        assertThat(result).isEqualTo(Success)
        coVerify {
            saveService.saveContact(match { newContact -> newContact.contactDataSet.all { it.modelStatus == NEW } })
        }
    }

    @ParameterizedTest
    @MethodSource("getTargetContactTypes")
    fun `should change the contact-data from DELETED to UNCHANGED`(newType: ContactType) {
        val oldId = getContactIdForOtherType(newType)
        val contactData = listOf(somePhoneNumber(modelStatus = DELETED))
        val contact = someContactEditableByIdType(id = oldId, contactData = contactData)
        coEvery { saveService.saveContact(any()) } returns Success
        coEvery { saveService.deleteContact(any()) } returns ContactDeleteResult.Success

        val result = runBlocking { underTest.changeContactType(contact, newType) }

        assertThat(result).isEqualTo(Success)
        coVerify {
            saveService.saveContact(
                match { newContact -> newContact.contactDataSet.all { it.modelStatus == UNCHANGED } }
            )
        }
    }

    @ParameterizedTest
    @MethodSource("getTargetContactTypes")
    fun `should delete the old contact`(newType: ContactType) {
        val oldType = getOtherType(newType)
        val oldId = getContactIdByType(oldType)
        val contact = someContactEditableByIdType(id = oldId)
        coEvery { saveService.saveContact(any()) } returns Success
        coEvery { saveService.deleteContact(any()) } returns ContactDeleteResult.Success

        val result = runBlocking { underTest.changeContactType(contact, newType) }

        assertThat(result).isEqualTo(Success)
        coVerify { saveService.deleteContact(match { it.id == oldId && it.type == oldType }) }
    }

    @ParameterizedTest
    @MethodSource("getTargetContactTypes")
    fun `should not delete the old contact if saving the new one fails and return the error`(newType: ContactType) {
        val oldId = getContactIdForOtherType(newType)
        val contact = someContactEditableByIdType(id = oldId)
        val saveErrors = listOf(UNKNOWN_ERROR)
        coEvery { saveService.saveContact(any()) } returns Failure(saveErrors)
        coEvery { saveService.deleteContact(any()) } returns ContactDeleteResult.Success
        val expectedErrors = listOf(UNABLE_TO_CREATE_CONTACT_WITH_NEW_TYPE) + saveErrors

        val result = runBlocking { underTest.changeContactType(contact, newType) }

        assertThat(result).isEqualTo(Failure(expectedErrors))
        coVerify(exactly = 0) { saveService.deleteContact(any()) }
    }

    @ParameterizedTest
    @MethodSource("getTargetContactTypes")
    fun `should return the error if deleting the old contact fails`(newType: ContactType) {
        val oldId = getContactIdForOtherType(newType)
        val contact = someContactEditableByIdType(id = oldId)
        val deleteErrors = listOf(UNKNOWN_ERROR)
        coEvery { saveService.saveContact(any()) } returns Success
        coEvery { saveService.deleteContact(any()) } returns ContactDeleteResult.Failure(deleteErrors)
        val expectedErrors = listOf(UNABLE_TO_DELETE_CONTACT_WITH_OLD_TYPE) + deleteErrors

        val result = runBlocking { underTest.changeContactType(contact, newType) }

        assertThat(result).isEqualTo(Failure(expectedErrors))
        coVerify { saveService.saveContact(any()) }
    }

    @ParameterizedTest
    @MethodSource("getTargetContactTypes")
    fun `should change contactDataIds to the correct ones`(newType: ContactType) {
        val oldId = getContactIdForOtherType(newType)
        val contactData = someListOfContactDataByIdType(oldId)
        val contact = someContactEditableByIdType(id = oldId, contactData = contactData)
        coEvery { saveService.saveContact(any()) } returns Success
        coEvery { saveService.deleteContact(any()) } returns ContactDeleteResult.Success

        val result = runBlocking { underTest.changeContactType(contact, newType) }

        assertThat(result).isEqualTo(Success)
        val slot = slot<IContactEditable>()
        coVerify { saveService.saveContact(capture(slot)) }
        assertThat(slot.isCaptured).isTrue
        slot.captured.contactDataSet.forEach {
            val correctIdType = when (newType) {
                SECRET -> IContactDataIdInternal::class.java
                PUBLIC -> IContactDataIdExternal::class.java
            }
            assertThat(it.id).isInstanceOf(correctIdType)
        }
    }

    @Test
    fun `should change the type of multiple contacts to SECRET`() {
        val contactIds = listOf(
            someExternalContactId(contactNo = 123), // success
            someExternalContactId(contactNo = 234), // error when resolving
            someExternalContactId(contactNo = 456), // error when saving
            someExternalContactId(contactNo = 789), // error when deleting
            someExternalContactId(contactNo = 666), // throws an exception
        )
        `should change the type of multiple contacts - shared implementation`(contactIds)
    }

    @Test
    fun `should change the type of multiple contacts to PUBLIC`() {
        val contactIds = listOf(
            someContactId(), // success
            someContactId(), // error when resolving
            someContactId(), // error when saving
            someContactId(), // error when deleting
            someContactId(), // throws an exception
        )
        `should change the type of multiple contacts - shared implementation`(contactIds)
    }

    /** expectation: the list contains exactly five ids */
    private fun `should change the type of multiple contacts - shared implementation`(contactIds: List<ContactId>) {
        assertThat(contactIds)
            .withFailMessage("Invalid setup: need 5 IDs but got ${contactIds.size}")
            .hasSize(5)
        val contacts = contactIds.map { someContactEditableByIdType(id = it) }
        val resolvedContacts = contacts
            .associateBy { it.id }
            .mapValues { (_, contact) -> contact.takeUnless { it.id == contactIds[1] } }
        coEvery { loadService.resolveContacts(any()) } returns resolvedContacts
        coEvery { saveService.saveContact(any()) } answers {
            val contact = firstArg<IContactEditable>()
            when (contact.id) {
                in listOf(contactIds[0], contactIds[3]) -> Success
                contactIds[4] -> throw Exception("Some Test Exception")
                else -> Failure(UNKNOWN_ERROR)
            }
        }
        coEvery { saveService.deleteContact(any()) } answers {
            val contact = firstArg<IContactBase>()
            if (contact.id in listOf(contactIds[0], contactIds[2])) ContactDeleteResult.Success
            else ContactDeleteResult.Failure(UNKNOWN_ERROR)
        }

        val result = runBlocking { underTest.changeContactType(contacts, SECRET) }

        assertThat(result.completelyFailed).isFalse
        assertThat(result.completelySuccessful).isFalse
        assertThat(result.successfulChanges).isEqualTo(listOf(contactIds[0]))
        // the contact which could not be resolved will just be ignored
        assertThat(result.failedChanges.keys).isEqualTo(setOf(contactIds[2], contactIds[3], contactIds[4]))
    }

    companion object {
        @JvmStatic
        private fun getTargetContactTypes(): List<ContactType> = listOf(PUBLIC, SECRET)

        private fun getOtherType(newType: ContactType): ContactType =
            getTargetContactTypes().first { it != newType }

        private fun getContactIdByType(type: ContactType): ContactId =
            when (type) {
                SECRET -> someInternalContactId()
                PUBLIC -> someExternalContactId()
            }

        private fun getContactIdForOtherType(type: ContactType): ContactId =
            getContactIdByType(getOtherType(type))

        private fun someListOfContactDataByIdType(id: ContactId): List<ContactData> =
            when (id) {
                is IContactIdExternal -> someListOfExternalContactData()
                is IContactIdInternal -> someListOfContactData()
            }
    }
}
