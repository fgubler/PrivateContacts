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
import ch.abwesend.privatecontacts.domain.model.contact.ContactType.PUBLIC
import ch.abwesend.privatecontacts.domain.model.contact.ContactType.SECRET
import ch.abwesend.privatecontacts.domain.model.contact.IContactBase
import ch.abwesend.privatecontacts.domain.model.contact.IContactEditable
import ch.abwesend.privatecontacts.domain.model.contact.isExternal
import ch.abwesend.privatecontacts.domain.model.result.ContactChangeError.NOT_YET_IMPLEMENTED_FOR_INTERNAL_CONTACTS
import ch.abwesend.privatecontacts.domain.model.result.ContactChangeError.UNABLE_TO_CREATE_CONTACT_WITH_NEW_TYPE
import ch.abwesend.privatecontacts.domain.model.result.ContactChangeError.UNABLE_TO_DELETE_CONTACT_WITH_OLD_TYPE
import ch.abwesend.privatecontacts.domain.model.result.ContactChangeError.UNKNOWN_ERROR
import ch.abwesend.privatecontacts.domain.model.result.ContactDeleteResult
import ch.abwesend.privatecontacts.domain.model.result.ContactSaveResult.Failure
import ch.abwesend.privatecontacts.domain.model.result.ContactSaveResult.Success
import ch.abwesend.privatecontacts.domain.repository.IContactGroupRepository
import ch.abwesend.privatecontacts.testutil.TestBase
import ch.abwesend.privatecontacts.testutil.databuilders.someContactEditable
import ch.abwesend.privatecontacts.testutil.databuilders.someContactImage
import ch.abwesend.privatecontacts.testutil.databuilders.someEmailAddress
import ch.abwesend.privatecontacts.testutil.databuilders.someExternalContactEditable
import ch.abwesend.privatecontacts.testutil.databuilders.someExternalContactId
import ch.abwesend.privatecontacts.testutil.databuilders.somePhoneNumber
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
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

    @Test
    fun `should fail for making the type PUBLIC`() {
        val oldType = SECRET
        val newType = PUBLIC
        val contact = someContactEditable(type = oldType)

        val result = runBlocking { underTest.changeContactType(contact, newType) }

        confirmVerified(saveService)
        assertThat(result).isEqualTo(Failure(NOT_YET_IMPLEMENTED_FOR_INTERNAL_CONTACTS))
    }

    @Test
    @Disabled("Changed the logic to change the type of the ID later")
    fun `should change the ID to ContactIdInternal`() {
        val newType = SECRET
        val externalId = someExternalContactId()
        val contact = someExternalContactEditable(id = externalId)
        assertThat(contact.isExternal).isTrue
        coEvery { saveService.saveContact(any()) } returns Success
        coEvery { saveService.deleteContact(any()) } returns ContactDeleteResult.Success

        val result = runBlocking { underTest.changeContactType(contact, newType) }

        assertThat(result).isEqualTo(Success)
        coVerify { saveService.saveContact(match { !it.isExternal }) }
    }

    @Test
    fun `should change the type to SECRET`() {
        val newType = SECRET
        val externalId = someExternalContactId()
        val contact = someExternalContactEditable(id = externalId)
        coEvery { saveService.saveContact(any()) } returns Success
        coEvery { saveService.deleteContact(any()) } returns ContactDeleteResult.Success

        val result = runBlocking { underTest.changeContactType(contact, newType) }

        assertThat(result).isEqualTo(Success)
        coVerify { saveService.saveContact(match { it.type == SECRET }) }
    }

    @Test
    fun `should change the contact-image from UNCHANGED to NEW`() {
        val newType = SECRET
        val externalId = someExternalContactId()
        val contact = someExternalContactEditable(id = externalId, image = someContactImage(modelStatus = UNCHANGED))
        coEvery { saveService.saveContact(any()) } returns Success
        coEvery { saveService.deleteContact(any()) } returns ContactDeleteResult.Success

        val result = runBlocking { underTest.changeContactType(contact, newType) }

        assertThat(result).isEqualTo(Success)
        coVerify { saveService.saveContact(match { it.image.modelStatus == NEW }) }
    }

    @Test
    fun `should change the contact-image from DELETED to UNCHANGED`() {
        val newType = SECRET
        val externalId = someExternalContactId()
        val contact = someExternalContactEditable(id = externalId, image = someContactImage(modelStatus = DELETED))
        coEvery { saveService.saveContact(any()) } returns Success
        coEvery { saveService.deleteContact(any()) } returns ContactDeleteResult.Success

        val result = runBlocking { underTest.changeContactType(contact, newType) }

        assertThat(result).isEqualTo(Success)
        coVerify { saveService.saveContact(match { it.image.modelStatus == UNCHANGED }) }
    }

    @Test
    fun `should change the contact-data from UNCHANGED and CHANGED to NEW`() {
        val newType = SECRET
        val externalId = someExternalContactId()
        val contactData = listOf(somePhoneNumber(modelStatus = UNCHANGED), someEmailAddress(modelStatus = CHANGED))
        val contact = someExternalContactEditable(id = externalId, contactData = contactData)
        coEvery { saveService.saveContact(any()) } returns Success
        coEvery { saveService.deleteContact(any()) } returns ContactDeleteResult.Success

        val result = runBlocking { underTest.changeContactType(contact, newType) }

        assertThat(result).isEqualTo(Success)
        coVerify {
            saveService.saveContact(match { newContact -> newContact.contactDataSet.all { it.modelStatus == NEW } })
        }
    }

    @Test
    fun `should change the contact-data from DELETED to UNCHANGED`() {
        val newType = SECRET
        val externalId = someExternalContactId()
        val contactData = listOf(somePhoneNumber(modelStatus = DELETED))
        val contact = someExternalContactEditable(id = externalId, contactData = contactData)
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

    @Test
    fun `should delete the old contact`() {
        val oldType = PUBLIC
        val newType = SECRET
        val externalId = someExternalContactId()
        val contact = someExternalContactEditable(id = externalId, type = oldType)
        coEvery { saveService.saveContact(any()) } returns Success
        coEvery { saveService.deleteContact(any()) } returns ContactDeleteResult.Success

        val result = runBlocking { underTest.changeContactType(contact, newType) }

        assertThat(result).isEqualTo(Success)
        coVerify { saveService.deleteContact(match { it.id == externalId && it.type == oldType }) }
    }

    @Test
    fun `should not delete the old contact if saving the new one fails and return the error`() {
        val oldType = PUBLIC
        val newType = SECRET
        val externalId = someExternalContactId()
        val contact = someExternalContactEditable(id = externalId, type = oldType)
        val saveErrors = listOf(UNKNOWN_ERROR)
        coEvery { saveService.saveContact(any()) } returns Failure(saveErrors)
        coEvery { saveService.deleteContact(any()) } returns ContactDeleteResult.Success
        val expectedErrors = listOf(UNABLE_TO_CREATE_CONTACT_WITH_NEW_TYPE) + saveErrors

        val result = runBlocking { underTest.changeContactType(contact, newType) }

        assertThat(result).isEqualTo(Failure(expectedErrors))
        coVerify(exactly = 0) { saveService.deleteContact(any()) }
    }

    @Test
    fun `should return the error if deleting the old contact fails`() {
        val oldType = PUBLIC
        val newType = SECRET
        val externalId = someExternalContactId()
        val contact = someExternalContactEditable(id = externalId, type = oldType)
        val deleteErrors = listOf(UNKNOWN_ERROR)
        coEvery { saveService.saveContact(any()) } returns Success
        coEvery { saveService.deleteContact(any()) } returns ContactDeleteResult.Failure(deleteErrors)
        val expectedErrors = listOf(UNABLE_TO_DELETE_CONTACT_WITH_OLD_TYPE) + deleteErrors

        val result = runBlocking { underTest.changeContactType(contact, newType) }

        assertThat(result).isEqualTo(Failure(expectedErrors))
        coVerify { saveService.saveContact(any()) }
    }
    @Test
    fun `should change the type of multiple contacts`() {
        val contactIds = listOf(
            someExternalContactId(contactNo = 123), // success
            someExternalContactId(contactNo = 456), // error when saving
            someExternalContactId(contactNo = 789), // error when deleting
            someExternalContactId(contactNo = 666), // throws an exception
        )
        val contacts = contactIds.map { someContactEditable(id = it, type = PUBLIC) }
        coEvery { loadService.resolveContacts(any()) } returns contacts
        coEvery { saveService.saveContact(any()) } answers {
            val contact = firstArg<IContactEditable>()
            when (contact.id) {
                in listOf(contactIds[0], contactIds[2]) -> Success
                contactIds[3] -> throw Exception("Some Test Exception")
                else -> Failure(UNKNOWN_ERROR)
            }
        }
        coEvery { saveService.deleteContact(any()) } answers {
            val contact = firstArg<IContactBase>()
            if (contact.id in listOf(contactIds[0], contactIds[1])) ContactDeleteResult.Success
            else ContactDeleteResult.Failure(UNKNOWN_ERROR)
        }

        val result = runBlocking { underTest.changeContactType(contacts, SECRET) }

        assertThat(result.completelyFailed).isFalse
        assertThat(result.completelySuccessful).isFalse
        assertThat(result.successfulChanges).isEqualTo(listOf(contactIds[0]))
        assertThat(result.failedChanges.keys).isEqualTo(setOf(contactIds[1], contactIds[2], contactIds[3]))
    }
}
