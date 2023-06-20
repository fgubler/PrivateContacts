/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository.importexport

import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.model.importexport.VCardParseError
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
