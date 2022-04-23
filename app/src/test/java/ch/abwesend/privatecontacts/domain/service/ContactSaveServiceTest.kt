/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.service

import ch.abwesend.privatecontacts.domain.model.result.ContactSaveResult
import ch.abwesend.privatecontacts.domain.model.result.ContactSaveResult.ValidationFailure
import ch.abwesend.privatecontacts.domain.model.result.ContactValidationError.NAME_NOT_SET
import ch.abwesend.privatecontacts.domain.model.result.ContactValidationResult
import ch.abwesend.privatecontacts.domain.model.result.ContactValidationResult.Failure
import ch.abwesend.privatecontacts.domain.repository.IContactRepository
import ch.abwesend.privatecontacts.testutil.TestBase
import ch.abwesend.privatecontacts.testutil.someContactEditable
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.runs
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.core.module.Module

@ExperimentalCoroutinesApi
@ExtendWith(MockKExtension::class)
class ContactSaveServiceTest : TestBase() {
    @MockK
    private lateinit var contactRepository: IContactRepository

    @MockK
    private lateinit var validationService: ContactValidationService

    @MockK
    private lateinit var sanitizingService: ContactSanitizingService

    private lateinit var underTest: ContactSaveService

    override fun setup() {
        underTest = ContactSaveService()
        every { sanitizingService.sanitizeContact(any()) } just runs
    }

    override fun Module.setupKoinModule() {
        single { contactRepository }
        single { validationService }
        single { sanitizingService }
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
        val contact = someContactEditable(isNew = true)
        coEvery { validationService.validateContact(any()) } returns ContactValidationResult.Success
        coEvery { contactRepository.createContact(any()) } returns ContactSaveResult.Success

        val result = runBlocking { underTest.saveContact(contact) }

        coVerify { validationService.validateContact(contact) }
        coVerify { contactRepository.createContact(contact) }
        confirmVerified(contactRepository)
        assertThat(result).isEqualTo(ContactSaveResult.Success)
    }

    @Test
    fun `should update existing contact`() {
        val contact = someContactEditable(isNew = false)
        coEvery { validationService.validateContact(any()) } returns ContactValidationResult.Success
        coEvery { contactRepository.updateContact(any()) } returns ContactSaveResult.Success

        val result = runBlocking { underTest.saveContact(contact) }

        coVerify { validationService.validateContact(contact) }
        coVerify { contactRepository.updateContact(contact) }
        confirmVerified(contactRepository)
        assertThat(result).isEqualTo(ContactSaveResult.Success)
    }
}
