/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.service

import ch.abwesend.privatecontacts.domain.model.contact.ContactAccount
import ch.abwesend.privatecontacts.domain.model.contact.ContactAccount.LocalPhoneContacts
import ch.abwesend.privatecontacts.domain.model.contact.ContactId
import ch.abwesend.privatecontacts.domain.model.contact.ContactIdInternal
import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.model.contact.ContactType.PUBLIC
import ch.abwesend.privatecontacts.domain.model.contact.IContactEditable
import ch.abwesend.privatecontacts.domain.model.importexport.VCardParseError.FILE_READING_FAILED
import ch.abwesend.privatecontacts.domain.model.importexport.VCardParseError.VCF_PARSING_FAILED
import ch.abwesend.privatecontacts.domain.model.result.ContactChangeError.UNKNOWN_ERROR
import ch.abwesend.privatecontacts.domain.model.result.ContactSaveResult
import ch.abwesend.privatecontacts.domain.model.result.ContactValidationError.NAME_NOT_SET
import ch.abwesend.privatecontacts.domain.model.result.batch.ContactIdBatchChangeResult
import ch.abwesend.privatecontacts.domain.model.result.generic.ErrorResult
import ch.abwesend.privatecontacts.domain.model.result.generic.SuccessResult
import ch.abwesend.privatecontacts.domain.repository.IContactRepository
import ch.abwesend.privatecontacts.domain.service.interfaces.IVCardImportExportRepository
import ch.abwesend.privatecontacts.testutil.TestBase
import ch.abwesend.privatecontacts.testutil.databuilders.someContactEditable
import ch.abwesend.privatecontacts.testutil.databuilders.someContactId
import ch.abwesend.privatecontacts.testutil.databuilders.someFileContent
import ch.abwesend.privatecontacts.testutil.databuilders.someImportId
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
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.koin.core.module.Module
import java.util.UUID

@ExperimentalCoroutinesApi
@ExtendWith(MockKExtension::class)
class ContactImportServiceTest : TestBase() {
    @MockK
    private lateinit var importExportRepository: IVCardImportExportRepository

    @MockK
    private lateinit var fileReadService: FileReadWriteService

    @MockK
    private lateinit var contactSaveService: ContactSaveService

    @MockK
    private lateinit var contactRepository: IContactRepository

    @InjectMockKs
    private lateinit var underTest: ContactImportService

    override fun setupKoinModule(module: Module) {
        super.setupKoinModule(module)
        module.single { importExportRepository }
        module.single { fileReadService }
        module.single { contactSaveService }
        module.single { contactRepository }
    }

    @Test
    fun `should return error if file-reading fails`() {
        val file = someUri()
        val targetType = ContactType.SECRET
        val targetAccount = ContactAccount.None
        val replaceExisting = false
        coEvery { fileReadService.readFileContent(any()) } returns ErrorResult(RuntimeException("Test"))

        val result = runBlocking { underTest.importContacts(file, targetType, targetAccount, replaceExisting) }

        coVerify { fileReadService.readFileContent(file) }
        assertThat(result).isInstanceOf(ErrorResult::class.java)
        assertThat(result.getErrorOrNull()).isEqualTo(FILE_READING_FAILED)
    }

    @Test
    fun `should forward error from vcf-parsing if that fails`() {
        val file = someUri()
        val targetType = ContactType.SECRET
        val targetAccount = ContactAccount.None
        val replaceExisting = false
        val fileContent = someFileContent()
        val error = VCF_PARSING_FAILED
        coEvery { fileReadService.readFileContent(any()) } returns SuccessResult(fileContent)
        coEvery { importExportRepository.parseContacts(any(), any()) } returns ErrorResult(error)

        val result = runBlocking { underTest.importContacts(file, targetType, targetAccount, replaceExisting) }

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
        val replaceExisting = false
        val contact = someContactEditable(type = PUBLIC, saveInAccount = LocalPhoneContacts)
        val parsedData = someParsedData(listOf(contact))
        coEvery { fileReadService.readFileContent(any()) } returns SuccessResult(fileContent)
        coEvery { importExportRepository.parseContacts(any(), any()) } returns SuccessResult(parsedData)
        coEvery { contactSaveService.saveContacts(any()) } answers {
            val contacts: List<IContactEditable> = firstArg()
            contacts.associateWith { ContactSaveResult.Success }
        }

        val result = runBlocking { underTest.importContacts(file, targetType, targetAccount, replaceExisting) }

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
        val replaceExisting = false
        val fileContent = someFileContent()
        val contacts = listOf(
            someContactEditable(firstName = "A", type = PUBLIC, saveInAccount = LocalPhoneContacts),
            someContactEditable(firstName = "B", type = PUBLIC, saveInAccount = LocalPhoneContacts),
            someContactEditable(firstName = "C", type = PUBLIC, saveInAccount = LocalPhoneContacts),
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

        val result = runBlocking { underTest.importContacts(file, targetType, targetAccount, replaceExisting) }

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

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `should replace successfully saved existing contacts and return save-results`(replaceExisting: Boolean) {
        val file = someUri()
        val targetType = ContactType.SECRET
        val targetAccount = ContactAccount.None
        val fileContent = someFileContent()
        val importUuid1 = UUID.randomUUID()
        val importUuid2 = UUID.randomUUID()
        val importUuid3 = UUID.randomUUID()
        val importUuid4 = UUID.randomUUID()
        val contactWithSaveError = someContactEditable(
            importId = someImportId(importUuid4),
            firstName = "E-fullMatch-butSaveError",
        )
        val contactsToImportSuccessfully = listOf(
            someContactEditable(importId = someImportId(), firstName = "A-noMatch"),
            someContactEditable(importId = someImportId(importUuid1), firstName = "B-fullMatch"),
            someContactEditable(importId = someImportId(importUuid2), firstName = "C-fullMatch", lastName = "C2"),
            someContactEditable(importId = someImportId(importUuid3), firstName = "D-partialMatch"),
        )
        val contactsToImport = contactsToImportSuccessfully + contactWithSaveError
        val matchingExistingContact1 = someContactEditable(
            importId = someImportId(importUuid1),
            firstName = "B-fullMatch",
        )
        val matchingExistingContact2 = someContactEditable(
            id = ContactIdInternal(importUuid2),
            firstName = "C-fullMatch",
            lastName = "C2",
        )
        val existingContacts = listOf(
            someContactEditable(id = someContactId(), importId = null, firstName = "A-noMatch"),
            matchingExistingContact1,
            matchingExistingContact2,
            someContactEditable(id = someContactId(), importId = someImportId(importUuid3), firstName = "D-diffName"),
            someContactEditable(importId = someImportId(importUuid4), firstName = "E-fullMatch-butSaveError"),
        )
        val parsedData = someParsedData(successfulContacts = contactsToImport)
        coEvery { fileReadService.readFileContent(any()) } returns SuccessResult(fileContent)
        coEvery { importExportRepository.parseContacts(any(), any()) } returns SuccessResult(parsedData)
        coEvery { contactRepository.resolveMatchingContacts(any()) } returns existingContacts
        coEvery { contactSaveService.deleteContacts(any()) } answers {
            val contactIds: Set<ContactId> = firstArg()
            ContactIdBatchChangeResult(successfulChanges = contactIds.toList(), failedChanges = emptyMap())
        }
        coEvery { contactSaveService.saveContacts(any()) } answers {
            val savingContacts: List<IContactEditable> = firstArg()
            savingContacts.associateWith {
                if (it == contactWithSaveError) ContactSaveResult.Failure(UNKNOWN_ERROR)
                else ContactSaveResult.Success
            }
        }

        val result = runBlocking { underTest.importContacts(file, targetType, targetAccount, replaceExisting) }

        coVerify { contactSaveService.saveContacts(contactsToImport) }
        assertThat(result).isInstanceOf(SuccessResult::class.java)
        val resultData = result.getValueOrNull()!!
        assertThat(resultData.newImportedContacts).hasSize(4)
        assertThat(resultData.importFailures).hasSize(1)
        assertThat(resultData.importValidationFailures).hasSize(0)
        assertThat(resultData.newImportedContacts).isEqualTo(contactsToImportSuccessfully)
        assertThat(resultData.importFailures.keys).containsExactly(contactWithSaveError)

        if (replaceExisting) {
            assertThat(resultData.replacedExistingContacts).isNotEmpty.hasSize(2)
            assertThat(resultData.replacedExistingContacts).containsExactly(
                matchingExistingContact1,
                matchingExistingContact2
            )
            val successFullImportIds = contactsToImport
                .filter { it != contactWithSaveError } // should not replace contacts which failed to import
                .mapNotNull { it.importId }
            val contactIdsToDelete = setOf(matchingExistingContact1.id, matchingExistingContact2.id)
            coVerify { contactRepository.resolveMatchingContacts(successFullImportIds) }
            coVerify { contactSaveService.deleteContacts(contactIdsToDelete) }
        } else {
            assertThat(resultData.replacedExistingContacts).isEmpty()
            coVerify(exactly = 0) { contactRepository.resolveMatchingContacts(any()) }
            coVerify(exactly = 0) { contactSaveService.deleteContacts(any()) }
        }
    }
}
