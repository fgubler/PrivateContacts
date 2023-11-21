/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository.importexport

import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.model.importexport.VCardCreateError
import ch.abwesend.privatecontacts.domain.model.importexport.VCardParseError
import ch.abwesend.privatecontacts.domain.model.importexport.VCardVersion
import ch.abwesend.privatecontacts.domain.model.result.generic.ErrorResult
import ch.abwesend.privatecontacts.domain.model.result.generic.SuccessResult
import ch.abwesend.privatecontacts.infrastructure.repository.vcard.mapping.ContactToVCardMapper
import ch.abwesend.privatecontacts.infrastructure.repository.vcard.mapping.VCardToContactMapper
import ch.abwesend.privatecontacts.infrastructure.repository.vcard.repository.VCardImportExportRepository
import ch.abwesend.privatecontacts.infrastructure.repository.vcard.repository.VCardRepository
import ch.abwesend.privatecontacts.testutil.RepositoryTestBase
import ch.abwesend.privatecontacts.testutil.databuilders.someContactEditable
import ch.abwesend.privatecontacts.testutil.databuilders.someFileContent
import ch.abwesend.privatecontacts.testutil.databuilders.someVCard
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
class VCardImportExportRepositoryTest : RepositoryTestBase() {
    @MockK
    private lateinit var parsingRepository: VCardRepository

    @MockK
    private lateinit var toVCardMapper: ContactToVCardMapper

    @MockK
    private lateinit var fromVCardMapper: VCardToContactMapper

    @InjectMockKs
    private lateinit var underTest: VCardImportExportRepository

    override fun setupKoinModule(module: Module) {
        super.setupKoinModule(module)
        module.single { parsingRepository }
        module.single { toVCardMapper }
        module.single { fromVCardMapper }
    }

    @Test
    fun `should export mapped VCard successfully`() {
        val contact = someContactEditable()
        val vCard = someVCard()
        val vCardVersion = VCardVersion.V4
        val createdFileContent = someFileContent()
        coEvery { toVCardMapper.mapToVCard(any()) } returns SuccessResult(vCard)
        coEvery { parsingRepository.exportVCards(any(), any()) } returns createdFileContent

        val result = runBlocking { underTest.exportContacts(listOf(contact), vCardVersion) }

        coVerify { toVCardMapper.mapToVCard(contact) }
        coVerify { parsingRepository.exportVCards(listOf(vCard), vCardVersion) }
        assertThat(result).isInstanceOf(SuccessResult::class.java)
        assertThat(result.getValueOrNull()!!.failedContacts).isEmpty()
        assertThat(result.getValueOrNull()!!.fileContent).isEqualTo(createdFileContent)
    }

    @Test
    fun `should return failed contact successfully if mapping fails`() {
        val failedContact = someContactEditable(firstName = "Failed")
        val successfulContact = someContactEditable(firstName = "Successful")
        val successfulVCard = someVCard()
        val vCardVersion = VCardVersion.V4
        coEvery { toVCardMapper.mapToVCard(failedContact) } returns ErrorResult(failedContact)
        coEvery { toVCardMapper.mapToVCard(successfulContact) } returns SuccessResult(successfulVCard)
        coEvery { parsingRepository.exportVCards(any(), any()) } returns someFileContent()

        val result = runBlocking { underTest.exportContacts(listOf(successfulContact, failedContact), vCardVersion) }

        coVerify { toVCardMapper.mapToVCard(failedContact) }
        coVerify { parsingRepository.exportVCards(listOf(successfulVCard), vCardVersion) }
        assertThat(result).isInstanceOf(SuccessResult::class.java)
        val resultValue = result.getValueOrNull()
        assertThat(resultValue).isNotNull
        assertThat(resultValue!!.failedContacts).containsExactly(failedContact)
    }

    @Test
    fun `export should fail if all contacts fail to be mapped`() {
        val contact = someContactEditable()
        val vCardVersion = VCardVersion.V4
        coEvery { toVCardMapper.mapToVCard(any()) } returns ErrorResult(contact)

        val result = runBlocking { underTest.exportContacts(listOf(contact), vCardVersion) }

        coVerify { toVCardMapper.mapToVCard(contact) }
        coVerify(exactly = 0) { parsingRepository.exportVCards(any(), any()) }
        assertThat(result).isInstanceOf(ErrorResult::class.java)
        assertThat(result.getValueOrNull()).isNull()
        assertThat(result.getErrorOrNull()).isNotNull.isEqualTo(VCardCreateError.NO_CONTACTS_TO_EXPORT)
    }

    @Test
    fun `should return ErrorResult if export fails`() {
        val contact = someContactEditable()
        val vCardVersion = VCardVersion.V4
        val vCard = someVCard()
        coEvery { toVCardMapper.mapToVCard(any()) } returns SuccessResult(vCard)
        coEvery { parsingRepository.exportVCards(any(), any()) } throws RuntimeException("Test")

        val result = runBlocking { underTest.exportContacts(listOf(contact), vCardVersion) }

        coVerify { toVCardMapper.mapToVCard(contact) }
        coVerify { parsingRepository.exportVCards(listOf(vCard), vCardVersion) }
        assertThat(result).isInstanceOf(ErrorResult::class.java)
        assertThat(result.getValueOrNull()).isNull()
        assertThat(result.getErrorOrNull()).isNotNull.isEqualTo(VCardCreateError.VCF_SERIALIZATION_FAILED)
    }

    @Test
    fun `should get parsed VCards from parsing repository`() {
        val fileContent = someFileContent()
        val targetType = ContactType.PUBLIC
        coEvery { parsingRepository.importVCards(any()) } returns emptyList()

        runBlocking { underTest.parseContacts(fileContent, targetType) }

        coVerify { parsingRepository.importVCards(fileContent) }
    }

    @Test
    fun `should return success if parsing succeeds`() {
        val fileContent = someFileContent()
        val targetType = ContactType.PUBLIC
        coEvery { parsingRepository.importVCards(any()) } returns emptyList()

        val result = runBlocking { underTest.parseContacts(fileContent, targetType) }

        assertThat(result).isInstanceOf(SuccessResult::class.java)
    }

    @Test
    fun `should return Error if parsing fails`() {
        val fileContent = someFileContent()
        val targetType = ContactType.PUBLIC
        coEvery { parsingRepository.importVCards(any()) } throws RuntimeException("Test")

        val result = runBlocking { underTest.parseContacts(fileContent, targetType) }

        assertThat(result).isInstanceOf(ErrorResult::class.java)
        assertThat(result.getValueOrNull()).isNull()
        assertThat(result.getErrorOrNull()).isNotNull.isEqualTo(VCardParseError.VCF_PARSING_FAILED)
    }

    @Test
    fun `should map vCard to contact`() {
        val fileContent = someFileContent()
        val targetType = ContactType.PUBLIC
        val vCard = someVCard()
        val contact = someContactEditable()
        coEvery { parsingRepository.importVCards(any()) } returns listOf(vCard)
        coEvery { fromVCardMapper.mapToContact(any(), any()) } returns SuccessResult(contact)

        val result = runBlocking { underTest.parseContacts(fileContent, targetType) }

        coVerify { fromVCardMapper.mapToContact(vCard, targetType) }
        assertThat(result).isInstanceOf(SuccessResult::class.java)
        assertThat(result.getValueOrNull()?.successfulContacts).isEqualTo(listOf(contact))
    }
}
