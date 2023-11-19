/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository.importexport

import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.model.contactdata.Company
import ch.abwesend.privatecontacts.domain.model.contactdata.EmailAddress
import ch.abwesend.privatecontacts.domain.model.contactdata.EventDate
import ch.abwesend.privatecontacts.domain.model.contactdata.PhoneNumber
import ch.abwesend.privatecontacts.domain.model.contactdata.PhysicalAddress
import ch.abwesend.privatecontacts.domain.model.contactdata.Relationship
import ch.abwesend.privatecontacts.domain.model.contactdata.Website
import ch.abwesend.privatecontacts.domain.model.result.generic.SuccessResult
import ch.abwesend.privatecontacts.domain.service.ContactSanitizingService
import ch.abwesend.privatecontacts.domain.service.interfaces.IAddressFormattingService
import ch.abwesend.privatecontacts.infrastructure.repository.vcard.mapping.VCardToContactMapper
import ch.abwesend.privatecontacts.infrastructure.repository.vcard.mapping.contactdata.import.ToPhysicalAddressMapper
import ch.abwesend.privatecontacts.infrastructure.service.AndroidContactCompanyMappingService
import ch.abwesend.privatecontacts.infrastructure.service.addressformatting.AddressFormattingService
import ch.abwesend.privatecontacts.testutil.RepositoryTestBase
import ch.abwesend.privatecontacts.testutil.databuilders.someCompanyVCardPseudoRelation
import ch.abwesend.privatecontacts.testutil.databuilders.someStructuredOrganization
import ch.abwesend.privatecontacts.testutil.databuilders.someVCard
import ch.abwesend.privatecontacts.testutil.databuilders.someVCardAddress
import ch.abwesend.privatecontacts.testutil.databuilders.someVCardAnniversary
import ch.abwesend.privatecontacts.testutil.databuilders.someVCardBirthday
import ch.abwesend.privatecontacts.testutil.databuilders.someVCardEmail
import ch.abwesend.privatecontacts.testutil.databuilders.someVCardPhoneNumber
import ch.abwesend.privatecontacts.testutil.databuilders.someVCardRelation
import ch.abwesend.privatecontacts.testutil.databuilders.someVCardUrl
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.core.module.Module
import java.time.LocalDate

@ExperimentalCoroutinesApi
@ExtendWith(MockKExtension::class)
class VCardToContactMapperTest : RepositoryTestBase() {

    @InjectMockKs
    private lateinit var underTest: VCardToContactMapper

    /** mocking these dependencies would be very close to re-implementing them... */
    override fun setupKoinModule(module: Module) {
        super.setupKoinModule(module)
        module.single<IAddressFormattingService> { AddressFormattingService() }
        module.single { ToPhysicalAddressMapper() }
        module.single { AndroidContactCompanyMappingService() }
        module.single { ContactSanitizingService() }
    }

    // TODO Add tests for all contact-data types which also test for the sort-order in the case of null

    @Test
    fun `should map phone numbers correctly`() {
        val vCard = someVCard()
        val phoneNumbers = listOf(
            someVCardPhoneNumber(number = "123"),
            someVCardPhoneNumber(number = ""),
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
    fun `should map email addresses correctly`() {
        val vCard = someVCard()
        val emailAddresses = listOf(
            someVCardEmail("a@b.c"),
            someVCardEmail(""),
            someVCardEmail(null),
            someVCardEmail("d@e.f"),
            someVCardEmail("g@h.i"),
        )
        val validEmailAddresses = emailAddresses.filter { it.value != null }
        vCard.emails.addAll(emailAddresses)

        val result = underTest.mapToContact(vCard, ContactType.PUBLIC)

        assertThat(result).isInstanceOf(SuccessResult::class.java)
        val resultContact = result.getValueOrNull()!!
        assertThat(resultContact.contactDataSet).hasSameSizeAs(validEmailAddresses)
        val resultEmailAddresses = resultContact.contactDataSet.filterIsInstance<EmailAddress>()
        assertThat(resultEmailAddresses).hasSameSizeAs(validEmailAddresses)
        assertThat(resultEmailAddresses.map { it.value }).containsExactlyElementsOf(validEmailAddresses.mapNotNull { it.value })
        assertThat(resultEmailAddresses.map { it.sortOrder }).containsExactlyElementsOf(validEmailAddresses.indices)
    }

    @Test
    fun `should map physical addresses correctly`() {
        val vCard = someVCard()
        val addresses = listOf(
            someVCardAddress(street = "Some Street"),
            someVCardAddress(street = "", locality = "Some Other City"),
            someVCardAddress(street = null),
            someVCardAddress(street = "Some Other Street", locality = "Some City"),
            someVCardAddress(street = null, locality = null, postalCode = null, region = null, country = null),
            someVCardAddress(street = "Some Other Street", locality = "Some Other City"),
        )
        val validAddresses = addresses.filter {
            !it.streetAddress.isNullOrEmpty() || it.locality != null ||
                it.postalCode != null || it.region != null || it.country != null
        }
        vCard.addresses.addAll(addresses)

        val result = underTest.mapToContact(vCard, ContactType.PUBLIC)

        assertThat(result).isInstanceOf(SuccessResult::class.java)
        val resultContact = result.getValueOrNull()!!
        assertThat(resultContact.contactDataSet).hasSameSizeAs(validAddresses)
        val resultAddresses = resultContact.contactDataSet.filterIsInstance<PhysicalAddress>()
        assertThat(resultAddresses).hasSameSizeAs(validAddresses)
        assertThat(resultAddresses.map { it.sortOrder }).containsExactlyElementsOf(validAddresses.indices)
        val resultStreets = resultAddresses.map { it.value }
        validAddresses.indices.forEach { index ->
            if (!validAddresses[index].streetAddress.isNullOrEmpty()) {
                assertThat(resultStreets[index]).startsWith(validAddresses[index].streetAddress)
            }
        }
    }

    @Test
    fun `should map relationships correctly`() {
        val vCard = someVCard()
        val relations = listOf(
            someVCardRelation("Marcus"),
            someVCardRelation(null),
            someVCardRelation(""),
            someVCardRelation("Aurelius"),
            someVCardRelation("Augustus"),
        )
        val validRelations = relations.filter { it.text != null }
        vCard.relations.addAll(relations)

        val result = underTest.mapToContact(vCard, ContactType.PUBLIC)

        assertThat(result).isInstanceOf(SuccessResult::class.java)
        val resultContact = result.getValueOrNull()!!
        assertThat(resultContact.contactDataSet).hasSameSizeAs(validRelations)
        val resultRelations = resultContact.contactDataSet.filterIsInstance<Relationship>()
        assertThat(resultRelations).hasSameSizeAs(validRelations)
        assertThat(resultRelations.map { it.value }).containsExactlyElementsOf(validRelations.mapNotNull { it.text })
        assertThat(resultRelations.map { it.sortOrder }).containsExactlyElementsOf(validRelations.indices)
    }

    @Test
    fun `should map websites correctly`() {
        val vCard = someVCard()
        val relations = listOf(
            someVCardUrl("http://www.google.com"),
            someVCardUrl(null),
            someVCardUrl(""),
            someVCardUrl("http://www.ergon.ch"),
        )
        val validRelations = relations.filter { it.value != null }
        vCard.urls.addAll(relations)

        val result = underTest.mapToContact(vCard, ContactType.PUBLIC)

        assertThat(result).isInstanceOf(SuccessResult::class.java)
        val resultContact = result.getValueOrNull()!!
        assertThat(resultContact.contactDataSet).hasSameSizeAs(validRelations)
        val resultRelations = resultContact.contactDataSet.filterIsInstance<Website>()
        assertThat(resultRelations).hasSameSizeAs(validRelations)
        assertThat(resultRelations.map { it.value }).containsExactlyElementsOf(validRelations.mapNotNull { it.value })
        assertThat(resultRelations.map { it.sortOrder }).containsExactlyElementsOf(validRelations.indices)
    }

    @Test
    fun `should map anniversaries correctly`() {
        val vCard = someVCard()
        val anniversaries = listOf(
            someVCardAnniversary(LocalDate.now()),
            someVCardAnniversary(null),
            someVCardAnniversary(LocalDate.now().minusDays(5)),
            someVCardAnniversary(LocalDate.now().plusMonths(1)),
        )
        val validAnniversaries = anniversaries.filter { it.date != null }
        vCard.anniversaries.addAll(anniversaries)

        val result = underTest.mapToContact(vCard, ContactType.PUBLIC)

        assertThat(result).isInstanceOf(SuccessResult::class.java)
        val resultContact = result.getValueOrNull()!!
        assertThat(resultContact.contactDataSet).hasSameSizeAs(validAnniversaries)
        val resultAnniversaries = resultContact.contactDataSet.filterIsInstance<EventDate>()
        assertThat(resultAnniversaries).hasSameSizeAs(validAnniversaries)
        assertThat(resultAnniversaries.map { it.sortOrder }).containsExactlyElementsOf(validAnniversaries.indices)
        assertThat(resultAnniversaries.map { it.value })
            .containsExactlyElementsOf(validAnniversaries.mapNotNull { it.date as? LocalDate })
    }

    @Test
    fun `should map birthdays correctly`() {
        val vCard = someVCard()
        val anniversaries = listOf(
            someVCardBirthday(LocalDate.now()),
            someVCardBirthday(null),
            someVCardBirthday(LocalDate.now().minusDays(5)),
            someVCardBirthday(LocalDate.now().plusMonths(1)),
        )
        val validBirthdays = anniversaries.filter { it.date != null }
        vCard.birthdays.addAll(anniversaries)

        val result = underTest.mapToContact(vCard, ContactType.PUBLIC)

        assertThat(result).isInstanceOf(SuccessResult::class.java)
        val resultContact = result.getValueOrNull()!!
        assertThat(resultContact.contactDataSet).hasSameSizeAs(validBirthdays)
        val resultBirthdays = resultContact.contactDataSet.filterIsInstance<EventDate>()
        assertThat(resultBirthdays).hasSameSizeAs(validBirthdays)
        assertThat(resultBirthdays.map { it.sortOrder }).containsExactlyElementsOf(validBirthdays.indices)
        assertThat(resultBirthdays.map { it.value })
            .containsExactlyElementsOf(validBirthdays.mapNotNull { it.date as? LocalDate })
    }

    @Test
    fun `should map company pseudo-relationships to companies`() {
        val vCard = someVCard()
        val companies = listOf(
            someCompanyVCardPseudoRelation("Google"),
            someCompanyVCardPseudoRelation(null),
            someCompanyVCardPseudoRelation(""),
            someCompanyVCardPseudoRelation("Amazon"),
            someCompanyVCardPseudoRelation("Ergon"),
        )
        val validCompanies = companies.filter { it.text != null }
        vCard.relations.addAll(companies)

        val result = underTest.mapToContact(vCard, ContactType.PUBLIC)

        assertThat(result).isInstanceOf(SuccessResult::class.java)
        val resultContact = result.getValueOrNull()!!
        assertThat(resultContact.contactDataSet).hasSameSizeAs(validCompanies)
        val resultCompanies = resultContact.contactDataSet.filterIsInstance<Company>()
        assertThat(resultCompanies).hasSameSizeAs(validCompanies)
        assertThat(resultCompanies.map { it.value }).containsExactlyElementsOf(validCompanies.mapNotNull { it.text })
        assertThat(resultCompanies.map { it.sortOrder }).containsExactlyElementsOf(validCompanies.indices)
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
