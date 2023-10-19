/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository.importexport

import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.model.contactdata.Company
import ch.abwesend.privatecontacts.domain.model.contactdata.PhoneNumber
import ch.abwesend.privatecontacts.domain.model.result.generic.SuccessResult
import ch.abwesend.privatecontacts.domain.service.ContactSanitizingService
import ch.abwesend.privatecontacts.infrastructure.repository.vcard.mapping.VCardToContactMapper
import ch.abwesend.privatecontacts.infrastructure.repository.vcard.mapping.contactdata.import.ToPhysicalAddressMapper
import ch.abwesend.privatecontacts.infrastructure.service.AndroidContactCompanyMappingService
import ch.abwesend.privatecontacts.testutil.RepositoryTestBase
import ch.abwesend.privatecontacts.testutil.databuilders.someStructuredOrganization
import ch.abwesend.privatecontacts.testutil.databuilders.someVCard
import ch.abwesend.privatecontacts.testutil.databuilders.someVCardPhoneNumber
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.core.module.Module

@ExperimentalCoroutinesApi
@ExtendWith(MockKExtension::class)
class VCardToContactMapperTest : RepositoryTestBase() {
    @MockK
    private lateinit var addressMapper: ToPhysicalAddressMapper

    @MockK
    private lateinit var companyMappingService: AndroidContactCompanyMappingService

    @InjectMockKs
    private lateinit var underTest: VCardToContactMapper

    override fun setupKoinModule(module: Module) {
        super.setupKoinModule(module)
        module.single { addressMapper }
        module.single { companyMappingService }
        module.single { ContactSanitizingService() }
    }

    // TODO Add tests for all contact-data types which also test for the sort-order in the case of null

    @Test
    fun `should map phone numbers correctly`() {
        val vCard = someVCard()
        val phoneNumbers = listOf(
            someVCardPhoneNumber(number = "123"),
            someVCardPhoneNumber(number = null),
            someVCardPhoneNumber(number = "456"),
        )
        val validPhoneNumbers = phoneNumbers.filter { it.text != null }
        vCard.telephoneNumbers.addAll(phoneNumbers)

        val result = underTest.mapToContact(vCard, ContactType.PUBLIC)

        assertThat(result).isInstanceOf(SuccessResult::class.java)
        val resultContact = result.getValueOrNull()!!
        assertThat(resultContact.contactDataSet).hasSameSizeAs(validPhoneNumbers)
        val resultPhoneNumbers = resultContact.contactDataSet.filterIsInstance<PhoneNumber>()
        assertThat(resultPhoneNumbers).hasSameSizeAs(validPhoneNumbers)
        assertThat(resultPhoneNumbers.map { it.value }).containsExactlyElementsOf(phoneNumbers.mapNotNull { it.text })
        assertThat(resultPhoneNumbers.map { it.sortOrder }).containsExactlyElementsOf(validPhoneNumbers.indices)
    }

    @Test
    fun `should map organizations to companies`() {
        val vCard = someVCard()
        val organizationNames = listOf(
            listOf("Company A"),
            listOf("Company B", "Department C"),
            emptyList(),
            listOf("Company D", "Department E", "Team F"), // these are hierarchical
        )
        val nonEmptyOrganizationNames = organizationNames.filter { it.isNotEmpty() }
        val numberOfOrganizations = nonEmptyOrganizationNames.size
        val organizations = organizationNames.map { names -> someStructuredOrganization(names) }
        vCard.organizations.addAll(organizations)
        val expectedCompanyNames = nonEmptyOrganizationNames.map { names -> names.joinToString(" - ") }

        val result = underTest.mapToContact(vCard, ContactType.PUBLIC)

        assertThat(result).isInstanceOf(SuccessResult::class.java)
        val resultContact = result.getValueOrNull()!!
        assertThat(resultContact.contactDataSet).hasSize(numberOfOrganizations)
        val companies = resultContact.contactDataSet.filterIsInstance<Company>()
        assertThat(companies).hasSize(numberOfOrganizations)
        assertThat(companies.map { it.value }).containsExactlyElementsOf(expectedCompanyNames)
        assertThat(companies.map { it.sortOrder }).containsExactlyElementsOf(0 until numberOfOrganizations)
    }
}
