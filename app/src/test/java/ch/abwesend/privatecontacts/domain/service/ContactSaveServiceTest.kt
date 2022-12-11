/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.service

import ch.abwesend.privatecontacts.domain.model.contact.ContactType.PUBLIC
import ch.abwesend.privatecontacts.domain.model.contact.ContactType.SECRET
import ch.abwesend.privatecontacts.domain.model.contact.IContactIdInternal
import ch.abwesend.privatecontacts.domain.model.result.ContactDeleteResult
import ch.abwesend.privatecontacts.domain.model.result.ContactSaveResult
import ch.abwesend.privatecontacts.domain.model.result.ContactSaveResult.ValidationFailure
import ch.abwesend.privatecontacts.domain.model.result.ContactValidationError.NAME_NOT_SET
import ch.abwesend.privatecontacts.domain.model.result.ContactValidationResult
import ch.abwesend.privatecontacts.domain.model.result.ContactValidationResult.Failure
import ch.abwesend.privatecontacts.domain.model.result.batch.ContactBatchChangeResult
import ch.abwesend.privatecontacts.domain.repository.IAndroidContactSaveService
import ch.abwesend.privatecontacts.domain.repository.IContactRepository
import ch.abwesend.privatecontacts.testutil.TestBase
import ch.abwesend.privatecontacts.testutil.databuilders.someContactEditable
import ch.abwesend.privatecontacts.testutil.databuilders.someContactEditableGeneric
import ch.abwesend.privatecontacts.testutil.databuilders.someContactEditableWithId
import ch.abwesend.privatecontacts.testutil.databuilders.someContactId
import ch.abwesend.privatecontacts.testutil.databuilders.someExternalContactId
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
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

    private lateinit var underTest: ContactSaveService

    override fun setup() {
        super.setup()
        underTest = ContactSaveService()
        every { sanitizingService.sanitizeContact(any()) } just runs
    }

    override fun setupKoinModule(module: Module) {
        super.setupKoinModule(module)
        module.single { contactRepository }
        module.single { androidContactSaveService }
        module.single { validationService }
        module.single { sanitizingService }
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
    fun `should create new contact`() {
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
    fun `should update existing contact`() {
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
            ContactBatchChangeResult.success(firstArg())
        }
        coEvery { androidContactSaveService.deleteContacts(any()) } answers {
            ContactBatchChangeResult.success(firstArg())
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
