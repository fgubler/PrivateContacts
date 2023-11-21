/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.service

import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.model.importexport.VCardCreateError
import ch.abwesend.privatecontacts.domain.model.importexport.VCardCreateError.VCF_SERIALIZATION_FAILED
import ch.abwesend.privatecontacts.domain.model.importexport.VCardVersion
import ch.abwesend.privatecontacts.domain.model.result.generic.ErrorResult
import ch.abwesend.privatecontacts.domain.model.result.generic.SuccessResult
import ch.abwesend.privatecontacts.domain.service.interfaces.IVCardImportExportRepository
import ch.abwesend.privatecontacts.testutil.TestBase
import ch.abwesend.privatecontacts.testutil.databuilders.someContactEditable
import ch.abwesend.privatecontacts.testutil.databuilders.someCreatedVCards
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
class ContactExportServiceTest : TestBase() {
    @MockK
    private lateinit var contactLoadService: ContactLoadService

    @MockK
    private lateinit var fileWriteService: FileReadWriteService

    @MockK
    private lateinit var importExportRepository: IVCardImportExportRepository

    @InjectMockKs
    private lateinit var underTest: ContactExportService

    override fun setupKoinModule(module: Module) {
        super.setupKoinModule(module)
        module.single { importExportRepository }
        module.single { fileWriteService }
        module.single { contactLoadService }
    }

    @Test
    fun `should return error if file-writing fails`() {
        val file = someUri()
        val sourceType = ContactType.SECRET
        val vCardVersion = VCardVersion.V3
        val partialData = someCreatedVCards()
        coEvery { contactLoadService.loadFullContactsByType(any()) } returns emptyList()
        coEvery { importExportRepository.exportContacts(any(), any()) } returns SuccessResult(partialData)
        coEvery { fileWriteService.writeContentToFile(any(), any()) } returns ErrorResult(RuntimeException("Test"))

        val result = runBlocking { underTest.exportContacts(file, sourceType, vCardVersion) }

        coVerify { fileWriteService.writeContentToFile(partialData.fileContent, file) }
        assertThat(result).isInstanceOf(ErrorResult::class.java)
        assertThat(result.getErrorOrNull()).isEqualTo(VCardCreateError.FILE_WRITING_FAILED)
    }

    @Test
    fun `should return error if vcf creation fails`() {
        val file = someUri()
        val sourceType = ContactType.SECRET
        val vCardVersion = VCardVersion.V3
        val contact = someContactEditable()
        coEvery { contactLoadService.loadFullContactsByType(any()) } returns listOf(contact)
        coEvery { importExportRepository.exportContacts(any(), any()) } returns ErrorResult(VCF_SERIALIZATION_FAILED)

        val result = runBlocking { underTest.exportContacts(file, sourceType, vCardVersion) }

        coVerify { importExportRepository.exportContacts(listOf(contact), vCardVersion) }
        assertThat(result).isInstanceOf(ErrorResult::class.java)
        assertThat(result.getErrorOrNull()).isEqualTo(VCF_SERIALIZATION_FAILED)
    }

    @Test
    fun `should return success if export succeeds`() {
        val file = someUri()
        val sourceType = ContactType.SECRET
        val vCardVersion = VCardVersion.V3
        val contact = someContactEditable()
        val partialData = someCreatedVCards()
        coEvery { contactLoadService.loadFullContactsByType(any()) } returns listOf(contact)
        coEvery { importExportRepository.exportContacts(any(), any()) } returns SuccessResult(partialData)
        coEvery { fileWriteService.writeContentToFile(any(), any()) } returns SuccessResult(Unit)

        val result = runBlocking { underTest.exportContacts(file, sourceType, vCardVersion) }

        coVerify { importExportRepository.exportContacts(listOf(contact), vCardVersion) }
        coVerify { fileWriteService.writeContentToFile(partialData.fileContent, file) }
        assertThat(result).isInstanceOf(SuccessResult::class.java)
        assertThat(result.getValueOrNull()!!.failedContacts).isEqualTo(partialData.failedContacts)
        assertThat(result.getValueOrNull()!!.successfulContacts).isEqualTo(listOf(contact))
    }
}
