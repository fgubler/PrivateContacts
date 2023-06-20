/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.service

import ch.abwesend.privatecontacts.domain.model.contact.ContactType.PUBLIC
import ch.abwesend.privatecontacts.domain.model.contact.ContactType.SECRET
import ch.abwesend.privatecontacts.domain.model.contact.IContactIdExternal
import ch.abwesend.privatecontacts.domain.model.contact.IContactIdInternal
import ch.abwesend.privatecontacts.domain.model.contactdata.IContactDataIdExternal
import ch.abwesend.privatecontacts.domain.model.contactdata.IContactDataIdInternal
import ch.abwesend.privatecontacts.domain.model.result.ContactDeleteResult
import ch.abwesend.privatecontacts.domain.model.result.ContactSaveResult
import ch.abwesend.privatecontacts.domain.model.result.ContactSaveResult.ValidationFailure
import ch.abwesend.privatecontacts.domain.model.result.ContactValidationError.NAME_NOT_SET
import ch.abwesend.privatecontacts.domain.model.result.ContactValidationResult
import ch.abwesend.privatecontacts.domain.model.result.ContactValidationResult.Failure
import ch.abwesend.privatecontacts.domain.model.result.batch.ContactIdBatchChangeResult
import ch.abwesend.privatecontacts.domain.repository.IAndroidContactSaveService
import ch.abwesend.privatecontacts.domain.repository.IContactRepository
import ch.abwesend.privatecontacts.testutil.TestBase
import ch.abwesend.privatecontacts.testutil.databuilders.someContactEditable
import ch.abwesend.privatecontacts.testutil.databuilders.someContactEditableGeneric
import ch.abwesend.privatecontacts.testutil.databuilders.someContactEditableWithId
import ch.abwesend.privatecontacts.testutil.databuilders.someContactId
import ch.abwesend.privatecontacts.testutil.databuilders.someExternalContactEditable
import ch.abwesend.privatecontacts.testutil.databuilders.someExternalContactId
import ch.abwesend.privatecontacts.testutil.databuilders.someInternalContactId
import ch.abwesend.privatecontacts.testutil.databuilders.someListOfContactData
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.SpyK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.runs
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.koin.core.module.Module

@ExperimentalCoroutinesApi
@ExtendWith(MockKExtension::class)
class ContactSaveServiceTest : TestBase() {
    @MockK
    private lateinit var contactRepository: IContactRepository

    @MockK
    private lateinit var androidContactSaveService: IAndroidContactSaveService

    @MockK
    private lateinit var validationService: ContactValidationService

    @MockK
    private lateinit var sanitizingService: ContactSanitizingService

    @SpyK
    private var underTest: ContactSaveService = ContactSaveService()

    override fun setup() {
        super.setup()
        every { sanitizingService.sanitizeContact(any()) } just runs
    }

    override fun setupKoinModule(module: Module) {
        super.setupKoinModule(module)
        module.single { contactRepository }
        module.single { androidContactSaveService }
        module.single { validationService }
        module.single { sanitizingService }
    }

    // TODO replace this test when proper batch-processing is introduced
    @Test
    fun `saveContacts should call saveContact`() {
        val contacts = listOf(
            someContactEditable(isNew = true),
            someContactEditable(isNew = true),
            someExternalContactEditable(isNew = true)
        )
        coEvery { validationService.validateContact(any()) } returns ContactValidationResult.Success
        coEvery { contactRepository.createContact(any(), any()) } returns ContactSaveResult.Success
        coEvery { androidContactSaveService.createContact(any()) } returns ContactSaveResult.Success

        val results = runBlocking { underTest.saveContacts(contacts) }

        coVerify(exactly = contacts.size) { underTest.saveContact(any()) }
        assertThat(results).hasSize(3)
        contacts.forEach { contact ->
            val result = results[contact]
            coVerify { validationService.validateContact(contact) }
            (contact.id as? IContactIdInternal)?.let { internalId ->
                coVerify { contactRepository.createContact(internalId, contact) }
            }
            (contact.id as? IContactIdExternal)?.let { _ ->
                coVerify { androidContactSaveService.createContact(contact) }
            }
            assertThat(result).isEqualTo(ContactSaveResult.Success)
        }
        confirmVerified(contactRepository)
        confirmVerified(androidContactSaveService)
    }

    @Test
    fun `should not save if validation fails`() {
        val contact = someContactEditable()
        val validationErrors = listOf(NAME_NOT_SET)
        coEvery { validationService.validateContact(any()) } returns Failure(validationErrors)

        val result = runBlocking { underTest.saveContact(contact) }

        coVerify { validationService.validateContact(contact) }
        confirmVerified(contactRepository) // should not be called at all
        assertThat(result).isEqualTo(ValidationFailure(validationErrors))
    }

    @Test
    fun `should create new internal contact`() {
        val (contactId, contact) = someContactEditableWithId(isNew = true)
        coEvery { validationService.validateContact(any()) } returns ContactValidationResult.Success
        coEvery { contactRepository.createContact(any(), any()) } returns ContactSaveResult.Success

        val result = runBlocking { underTest.saveContact(contact) }

        coVerify { validationService.validateContact(contact) }
        coVerify { contactRepository.createContact(contactId, contact) }
        confirmVerified(contactRepository)
        assertThat(result).isEqualTo(ContactSaveResult.Success)
    }

    @Test
    fun `should update existing internal contact`() {
        val (contactId, contact) = someContactEditableWithId(isNew = false)
        coEvery { validationService.validateContact(any()) } returns ContactValidationResult.Success
        coEvery { contactRepository.updateContact(any(), any()) } returns ContactSaveResult.Success

        val result = runBlocking { underTest.saveContact(contact) }

        coVerify { validationService.validateContact(contact) }
        coVerify { contactRepository.updateContact(contactId, contact) }
        confirmVerified(contactRepository)
        assertThat(result).isEqualTo(ContactSaveResult.Success)
    }

    @Test
    fun `should treat external contact of type secret as new`() {
        val contactId = someExternalContactId()
        val contact = someContactEditableGeneric(id = contactId, isNew = false, type = SECRET)
        coEvery { validationService.validateContact(any()) } returns ContactValidationResult.Success
        coEvery { contactRepository.createContact(any(), any()) } returns ContactSaveResult.Success

        val result = runBlocking { underTest.saveContact(contact) }

        val slot = slot<IContactIdInternal>()
        coVerify { validationService.validateContact(contact) }
        coVerify { contactRepository.createContact(capture(slot), contact) }
        confirmVerified(contactRepository)
        assertThat(result).isEqualTo(ContactSaveResult.Success)
        assertThat(slot.isCaptured).isTrue
        val savedContactId = slot.captured
        assertThat(savedContactId).isInstanceOf(IContactIdInternal::class.java)
    }

    @Test
    fun `should change contact-data-IDs to EXTERNAL for type PUBLIC`() {
        val originalContactData = someListOfContactData(internalIds = true)
        val contactId = someInternalContactId()
        val contact = someContactEditableGeneric(
            id = contactId,
            isNew = false,
            type = PUBLIC,
            contactData = originalContactData,
        )
        coEvery { validationService.validateContact(any()) } returns ContactValidationResult.Success
        coEvery { androidContactSaveService.createContact(any()) } returns ContactSaveResult.Success

        val result = runBlocking { underTest.saveContact(contact) }

        assertThat(result).isEqualTo(ContactSaveResult.Success)
        val newContactData = contact.contactDataSet // the mutable contact-object was changed in the meantime
        assertThat(originalContactData.all { it.id is IContactDataIdInternal }).isTrue // check test-setup
        assertThat(newContactData.all { it.id is IContactDataIdExternal }).isTrue
    }

    @Test
    fun `should change contact-data-IDs to INTERNAL for type PRIVATE`() {
        val originalContactData = someListOfContactData(internalIds = false)
        val contactId = someExternalContactId()
        val contact = someContactEditableGeneric(
            id = contactId,
            isNew = false,
            type = SECRET,
            contactData = originalContactData,
        )
        coEvery { validationService.validateContact(any()) } returns ContactValidationResult.Success
        coEvery { contactRepository.createContact(any(), any()) } returns ContactSaveResult.Success

        val result = runBlocking { underTest.saveContact(contact) }

        assertThat(result).isEqualTo(ContactSaveResult.Success)
        val newContactData = contact.contactDataSet // the mutable contact-object was changed in the meantime
        assertThat(originalContactData.all { it.id is IContactDataIdExternal }).isTrue // check test-setup
        assertThat(newContactData.all { it.id is IContactDataIdInternal }).isTrue
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `should update external contacts externally and internal ones internally`(isExternal: Boolean) {
        val externalContactId = someExternalContactId()
        val internalContactId = someContactId()
        val contactId = if (isExternal) externalContactId else internalContactId
        val contactType = if (isExternal) PUBLIC else SECRET
        val contact = someContactEditable(id = contactId, type = contactType, isNew = false)
        coEvery { validationService.validateContact(any()) } returns ContactValidationResult.Success
        coEvery { contactRepository.updateContact(any(), any()) } returns ContactSaveResult.Success
        coEvery { androidContactSaveService.updateContact(any(), any()) } returns ContactSaveResult.Success

        val result = runBlocking { underTest.saveContact(contact) }

        assertThat(result).isEqualTo(ContactSaveResult.Success)
        if (isExternal) {
            coVerify { androidContactSaveService.updateContact(externalContactId, contact) }
        } else {
            coVerify { contactRepository.updateContact(internalContactId, contact) }
        }
        confirmVerified(contactRepository)
        confirmVerified(androidContactSaveService)
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `should create new external contacts externally and internal ones internally`(isExternal: Boolean) {
        val externalContactId = someExternalContactId()
        val internalContactId = someContactId()
        val contactId = if (isExternal) externalContactId else internalContactId
        val contactType = if (isExternal) PUBLIC else SECRET
        val contact = someContactEditable(id = contactId, type = contactType, isNew = true)
        coEvery { validationService.validateContact(any()) } returns ContactValidationResult.Success
        coEvery { contactRepository.createContact(any(), any()) } returns ContactSaveResult.Success
        coEvery { androidContactSaveService.createContact(any()) } returns ContactSaveResult.Success

        val result = runBlocking { underTest.saveContact(contact) }

        assertThat(result).isEqualTo(ContactSaveResult.Success)
        if (isExternal) {
            coVerify { androidContactSaveService.createContact(contact) }
        } else {
            coVerify { contactRepository.createContact(internalContactId, contact) }
        }
        confirmVerified(contactRepository)
        confirmVerified(androidContactSaveService)
    }

    /**
     * If the ID does not match the type (i.e. external-ID for type SECRET and vice versa),
     * that means that we are changing the contact-type => consider as "new".
     */
    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `should create type-changing external contacts externally and internal ones internally`(isExternal: Boolean) {
        val externalContactId = someExternalContactId()
        val internalContactId = someContactId()
        val contactId = if (isExternal) internalContactId else externalContactId // the id does not match the type
        val contactType = if (isExternal) PUBLIC else SECRET
        val contact = someContactEditable(id = contactId, type = contactType, isNew = false)
        coEvery { validationService.validateContact(any()) } returns ContactValidationResult.Success
        coEvery { contactRepository.createContact(any(), any()) } returns ContactSaveResult.Success
        coEvery { androidContactSaveService.createContact(any()) } returns ContactSaveResult.Success

        val result = runBlocking { underTest.saveContact(contact) }

        assertThat(result).isEqualTo(ContactSaveResult.Success)
        if (isExternal) {
            coVerify { androidContactSaveService.createContact(contact) }
        } else {
            coVerify { contactRepository.createContact(any(), contact) }
        }
        confirmVerified(contactRepository)
        confirmVerified(androidContactSaveService)
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `should delete external contacts externally and internal ones internally`(isExternal: Boolean) {
        val externalContactId = someExternalContactId()
        val internalContactId = someContactId()
        val contactId = if (isExternal) externalContactId else internalContactId
        val contact = someContactEditable(id = contactId)
        coEvery { contactRepository.deleteContacts(any()) } answers {
            ContactIdBatchChangeResult.success(firstArg())
        }
        coEvery { androidContactSaveService.deleteContacts(any()) } answers {
            ContactIdBatchChangeResult.success(firstArg())
        }

        val resultSingle = runBlocking { underTest.deleteContact(contact) }
        val resultBatch = runBlocking { underTest.deleteContacts(setOf(contact.id)) }

        assertThat(resultSingle).isEqualTo(ContactDeleteResult.Success)
        assertThat(resultBatch.completelySuccessful).isTrue
        assertThat(resultBatch.successfulChanges).isEqualTo(listOf(contact.id))
        if (isExternal) {
            coVerify(exactly = 2) { androidContactSaveService.deleteContacts(listOf(externalContactId)) }
        } else {
            coVerify(exactly = 2) { contactRepository.deleteContacts(listOf(internalContactId)) }
        }
        confirmVerified(contactRepository)
        confirmVerified(androidContactSaveService)
    }
}
