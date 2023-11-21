/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.service

import ch.abwesend.privatecontacts.domain.model.contact.ContactAccount
import ch.abwesend.privatecontacts.domain.model.contact.ContactAccount.LocalPhoneContacts
import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.model.contact.IContactEditable
import ch.abwesend.privatecontacts.domain.model.importexport.VCardParseError.FILE_READING_FAILED
import ch.abwesend.privatecontacts.domain.model.importexport.VCardParseError.VCF_PARSING_FAILED
import ch.abwesend.privatecontacts.domain.model.result.ContactChangeError.UNKNOWN_ERROR
import ch.abwesend.privatecontacts.domain.model.result.ContactSaveResult
import ch.abwesend.privatecontacts.domain.model.result.ContactValidationError.NAME_NOT_SET
import ch.abwesend.privatecontacts.domain.model.result.generic.ErrorResult
import ch.abwesend.privatecontacts.domain.model.result.generic.SuccessResult
import ch.abwesend.privatecontacts.domain.service.interfaces.IVCardImportExportRepository
import ch.abwesend.privatecontacts.testutil.TestBase
import ch.abwesend.privatecontacts.testutil.databuilders.someContactEditable
import ch.abwesend.privatecontacts.testutil.databuilders.someFileContent
import ch.abwesend.privatecontacts.testutil.databuilders.someParsedData
import ch.abwesend.privatecontacts.testutil.databuilders.someUri
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.core.module.Module

@ExperimentalCoroutinesApi
@ExtendWith(MockKExtension::class)
class ContactImportServiceTest : TestBase() {
    @MockK
    private lateinit var importExportRepository: IVCardImportExportRepository

    @MockK
    private lateinit var fileReadService: FileReadWriteService

    @MockK
    private lateinit var contactSaveService: ContactSaveService

    @InjectMockKs
    private lateinit var underTest: ContactImportService

    override fun setupKoinModule(module: Module) {
        super.setupKoinModule(module)
        module.single { importExportRepository }
        module.single { fileReadService }
        module.single { contactSaveService }
    }

    @Test
    fun `should return error if file-reading fails`() {
        val file = someUri()
        val targetType = ContactType.SECRET
        val targetAccount = ContactAccount.None
        coEvery { fileReadService.readFileContent(any()) } returns ErrorResult(RuntimeException("Test"))

        val result = runBlocking { underTest.importContacts(file, targetType, targetAccount) }

        coVerify { fileReadService.readFileContent(file) }
        assertThat(result).isInstanceOf(ErrorResult::class.java)
        assertThat(result.getErrorOrNull()).isEqualTo(FILE_READING_FAILED)
    }

    @Test
    fun `should forward error from vcf-parsing if that fails`() {
        val file = someUri()
        val targetType = ContactType.SECRET
        val targetAccount = ContactAccount.None
        val fileContent = someFileContent()
        val error = VCF_PARSING_FAILED
        coEvery { fileReadService.readFileContent(any()) } returns SuccessResult(fileContent)
        coEvery { importExportRepository.parseContacts(any(), any()) } returns ErrorResult(error)

        val result = runBlocking { underTest.importContacts(file, targetType, targetAccount) }

        coVerify { importExportRepository.parseContacts(fileContent, targetType) }
        assertThat(result).isInstanceOf(ErrorResult::class.java)
        assertThat(result.getErrorOrNull()).isEqualTo(error)
    }

    @Test
    fun `should set the targetType and targetAccount properly`() {
        val file = someUri()
        val targetType = ContactType.SECRET
        val targetAccount = ContactAccount.None
        val fileContent = someFileContent()
        val contact = someContactEditable(type = ContactType.PUBLIC, saveInAccount = LocalPhoneContacts)
        val parsedData = someParsedData(listOf(contact))
        coEvery { fileReadService.readFileContent(any()) } returns SuccessResult(fileContent)
        coEvery { importExportRepository.parseContacts(any(), any()) } returns SuccessResult(parsedData)
        coEvery { contactSaveService.saveContacts(any()) } answers {
            val contacts: List<IContactEditable> = firstArg()
            contacts.associateWith { ContactSaveResult.Success }
        }

        val result = runBlocking { underTest.importContacts(file, targetType, targetAccount) }

        coVerify { importExportRepository.parseContacts(fileContent, targetType) }
        assertThat(result).isInstanceOf(SuccessResult::class.java)
        val importedContacts = result.getValueOrNull()?.newImportedContacts
        assertThat(importedContacts).isNotNull.hasSize(1)
        assertThat(importedContacts!!.first().type).isEqualTo(targetType)
        assertThat(importedContacts.first().saveInAccount).isEqualTo(targetAccount)
    }

    @Test
    fun `should save contacts and return save-results`() {
        val file = someUri()
        val targetType = ContactType.SECRET
        val targetAccount = ContactAccount.None
        val fileContent = someFileContent()
        val contacts = listOf(
            someContactEditable(firstName = "A", type = ContactType.PUBLIC, saveInAccount = LocalPhoneContacts),
            someContactEditable(firstName = "B", type = ContactType.PUBLIC, saveInAccount = LocalPhoneContacts),
            someContactEditable(firstName = "C", type = ContactType.PUBLIC, saveInAccount = LocalPhoneContacts),
        )
        val error = UNKNOWN_ERROR
        val validationError = NAME_NOT_SET
        val parsedData = someParsedData(successfulContacts = contacts)
        coEvery { fileReadService.readFileContent(any()) } returns SuccessResult(fileContent)
        coEvery { importExportRepository.parseContacts(any(), any()) } returns SuccessResult(parsedData)
        coEvery { contactSaveService.saveContacts(any()) } answers {
            val savingContacts: List<IContactEditable> = firstArg()
            mapOf(
                savingContacts[0] to ContactSaveResult.Success,
                savingContacts[1] to ContactSaveResult.Failure(error),
                savingContacts[2] to ContactSaveResult.ValidationFailure(listOf(validationError)),
            )
        }

        val result = runBlocking { underTest.importContacts(file, targetType, targetAccount) }

        coVerify { contactSaveService.saveContacts(contacts) }
        assertThat(result).isInstanceOf(SuccessResult::class.java)
        val resultData = result.getValueOrNull()!!
        assertThat(resultData.newImportedContacts).hasSize(1)
        assertThat(resultData.importFailures).hasSize(1)
        assertThat(resultData.importValidationFailures).hasSize(1)
        assertThat(resultData.newImportedContacts.first()).isEqualTo(contacts[0])
        assertThat(resultData.importFailures).containsKey(contacts[1])
        assertThat(resultData.importFailures[contacts[1]]).containsExactly(error)
        assertThat(resultData.importValidationFailures).containsKey(contacts[2])
        assertThat(resultData.importValidationFailures[contacts[2]]).containsExactly(validationError)
    }
}
