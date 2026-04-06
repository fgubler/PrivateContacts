/*
 * Private Contacts
 * Copyright (c) 2026.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository.contactsandroid.mapping

import ch.abwesend.privatecontacts.domain.model.ModelStatus
import ch.abwesend.privatecontacts.domain.model.contactdata.Company
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataIdAndroid
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType
import ch.abwesend.privatecontacts.domain.model.contactdata.EmailAddress
import ch.abwesend.privatecontacts.domain.model.contactdata.EventDate
import ch.abwesend.privatecontacts.domain.model.contactdata.PhoneNumber
import ch.abwesend.privatecontacts.domain.model.contactdata.PhysicalAddress
import ch.abwesend.privatecontacts.domain.model.contactdata.Relationship
import ch.abwesend.privatecontacts.domain.model.contactdata.Website
import ch.abwesend.privatecontacts.domain.service.interfaces.IAddressFormattingService
import ch.abwesend.privatecontacts.infrastructure.service.AndroidContactCompanyMappingService
import ch.abwesend.privatecontacts.infrastructure.service.addressformatting.AddressFormattingService
import ch.abwesend.privatecontacts.testutil.TestBase
import ch.abwesend.privatecontacts.testutil.databuilders.someContactsAndroidAddress
import ch.abwesend.privatecontacts.testutil.databuilders.someContactsAndroidContact
import ch.abwesend.privatecontacts.testutil.databuilders.someContactsAndroidEmail
import ch.abwesend.privatecontacts.testutil.databuilders.someContactsAndroidEvent
import ch.abwesend.privatecontacts.testutil.databuilders.someContactsAndroidEventDate
import ch.abwesend.privatecontacts.testutil.databuilders.someContactsAndroidOrganization
import ch.abwesend.privatecontacts.testutil.databuilders.someContactsAndroidPhone
import ch.abwesend.privatecontacts.testutil.databuilders.someContactsAndroidRawContact
import ch.abwesend.privatecontacts.testutil.databuilders.someContactsAndroidRelation
import ch.abwesend.privatecontacts.testutil.databuilders.someContactsAndroidWebsite
import contacts.core.entities.PhoneEntity
import contacts.core.entities.RelationEntity
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.core.module.Module

@ExperimentalCoroutinesApi
@ExtendWith(MockKExtension::class)
class ContactsAndroidDataMapperTest : TestBase() {
    private val addressFormattingService: IAddressFormattingService = AddressFormattingService()
    private val companyMappingService = AndroidContactCompanyMappingService()

    private lateinit var underTest: ContactsAndroidDataMapper

    override fun setup() {
        super.setup()
        underTest = ContactsAndroidDataMapper()
    }

    override fun setupKoinModule(module: Module) {
        super.setupKoinModule(module)
        module.single<IAddressFormattingService> { addressFormattingService }
        module.single { companyMappingService }
    }

    // ── Phone Numbers ──────────────────────────────────────────────────────────

    @Test
    fun `should map phone numbers correctly`() {
        val phones = listOf(
            someContactsAndroidPhone(id = 1L, number = "+41791234567", type = PhoneEntity.Type.MOBILE),
            someContactsAndroidPhone(id = 2L, number = "+41791234568", type = PhoneEntity.Type.WORK),
        )
        val rawContact = someContactsAndroidRawContact(phones = phones)
        val contact = someContactsAndroidContact(rawContacts = listOf(rawContact))

        val result = underTest.getContactData(contact)

        val phoneNumbers = result.filterIsInstance<PhoneNumber>()
        assertThat(phoneNumbers).hasSize(2)
        assertThat(phoneNumbers[0].value).isEqualTo("+41791234567")
        assertThat(phoneNumbers[0].type).isEqualTo(ContactDataType.Mobile)
        assertThat(phoneNumbers[0].sortOrder).isEqualTo(0)
        assertThat(phoneNumbers[0].id).isEqualTo(ContactDataIdAndroid(contactDataNo = 1L))
        assertThat(phoneNumbers[1].value).isEqualTo("+41791234568")
        assertThat(phoneNumbers[1].type).isEqualTo(ContactDataType.Business)
        assertThat(phoneNumbers[1].sortOrder).isEqualTo(1)
    }

    @Test
    fun `should sanitize away phone number with null number`() {
        val phones = listOf(someContactsAndroidPhone(number = null))
        val rawContact = someContactsAndroidRawContact(phones = phones)
        val contact = someContactsAndroidContact(rawContacts = listOf(rawContact))

        val result = underTest.getContactData(contact)

        val phoneNumbers = result.filterIsInstance<PhoneNumber>()
        assertThat(phoneNumbers).isEmpty()
    }

    @Test
    fun `should set isMain when phone isPrimary`() {
        val phones = listOf(someContactsAndroidPhone(isPrimary = true))
        val rawContact = someContactsAndroidRawContact(phones = phones)
        val contact = someContactsAndroidContact(rawContacts = listOf(rawContact))

        val result = underTest.getContactData(contact)

        val phoneNumbers = result.filterIsInstance<PhoneNumber>()
        assertThat(phoneNumbers).hasSize(1)
        assertThat(phoneNumbers[0].isMain).isTrue()
    }

    // ── Email Addresses ────────────────────────────────────────────────────────

    @Test
    fun `should map email addresses correctly`() {
        val emails = listOf(
            someContactsAndroidEmail(id = 10L, address = "luke@rebels.org"),
            someContactsAndroidEmail(id = 11L, address = "luke@jedi.org"),
        )
        val rawContact = someContactsAndroidRawContact(emails = emails)
        val contact = someContactsAndroidContact(rawContacts = listOf(rawContact))

        val result = underTest.getContactData(contact)

        val emailAddresses = result.filterIsInstance<EmailAddress>()
        assertThat(emailAddresses).hasSize(2)
        assertThat(emailAddresses[0].value).isEqualTo("luke@rebels.org")
        assertThat(emailAddresses[0].id).isEqualTo(ContactDataIdAndroid(contactDataNo = 10L))
        assertThat(emailAddresses[1].value).isEqualTo("luke@jedi.org")
    }

    // ── Physical Addresses ─────────────────────────────────────────────────────

    @Test
    fun `should map physical addresses correctly`() {
        val addresses = listOf(
            someContactsAndroidAddress(id = 20L, street = "123 Main St", city = "Mos Eisley"),
        )
        val rawContact = someContactsAndroidRawContact(addresses = addresses)
        val contact = someContactsAndroidContact(rawContacts = listOf(rawContact))

        val result = underTest.getContactData(contact)

        val physicalAddresses = result.filterIsInstance<PhysicalAddress>()
        assertThat(physicalAddresses).hasSize(1)
        assertThat(physicalAddresses[0].value).contains("123 Main St")
        assertThat(physicalAddresses[0].value).contains("Mos Eisley")
        assertThat(physicalAddresses[0].id).isEqualTo(ContactDataIdAndroid(contactDataNo = 20L))
    }

    // ── Websites ───────────────────────────────────────────────────────────────

    @Test
    fun `should map websites correctly`() {
        val websites = listOf(
            someContactsAndroidWebsite(id = 30L, url = "https://rebels.org"),
            someContactsAndroidWebsite(id = 31L, url = "https://jedi.org"),
        )
        val rawContact = someContactsAndroidRawContact(websites = websites)
        val contact = someContactsAndroidContact(rawContacts = listOf(rawContact))

        val result = underTest.getContactData(contact)

        val resultWebsites = result.filterIsInstance<Website>()
        assertThat(resultWebsites).hasSize(2)
        assertThat(resultWebsites[0].value).isEqualTo("https://rebels.org")
        assertThat(resultWebsites[0].type).isEqualTo(ContactDataType.Main)
        assertThat(resultWebsites[1].value).isEqualTo("https://jedi.org")
    }

    // ── Relationships ──────────────────────────────────────────────────────────

    @Test
    fun `should map relationships correctly`() {
        val relations = listOf(
            someContactsAndroidRelation(id = 40L, name = "Leia", type = RelationEntity.Type.SISTER),
            someContactsAndroidRelation(id = 41L, name = "Anakin", type = RelationEntity.Type.FATHER),
        )
        val rawContact = someContactsAndroidRawContact(relations = relations)
        val contact = someContactsAndroidContact(rawContacts = listOf(rawContact))

        val result = underTest.getContactData(contact)

        val relationships = result.filterIsInstance<Relationship>()
        assertThat(relationships).hasSize(2)
        assertThat(relationships[0].value).isEqualTo("Leia")
        assertThat(relationships[0].type).isEqualTo(ContactDataType.RelationshipSister)
        assertThat(relationships[1].value).isEqualTo("Anakin")
        assertThat(relationships[1].type).isEqualTo(ContactDataType.RelationshipFather)
    }

    @Test
    fun `should filter out pseudo-relation companies from relationships`() {
        val companyLabel = companyMappingService.encodeToPseudoRelationshipLabel(ContactDataType.Business)
        val relations = listOf(
            someContactsAndroidRelation(id = 40L, name = "Leia", type = RelationEntity.Type.SISTER),
            someContactsAndroidRelation(
                id = 41L,
                name = "Rebel Corp",
                type = RelationEntity.Type.CUSTOM,
                label = companyLabel,
            ),
        )
        val rawContact = someContactsAndroidRawContact(relations = relations)
        val contact = someContactsAndroidContact(rawContacts = listOf(rawContact))

        val result = underTest.getContactData(contact)

        val relationships = result.filterIsInstance<Relationship>()
        assertThat(relationships).hasSize(1)
        assertThat(relationships[0].value).isEqualTo("Leia")
    }

    // ── Event Dates ────────────────────────────────────────────────────────────

    @Test
    fun `should map event dates correctly`() {
        val eventDate = someContactsAndroidEventDate(year = 1990, month = 5, dayOfMonth = 15)
        val events = listOf(
            someContactsAndroidEvent(id = 50L, date = eventDate),
        )
        val rawContact = someContactsAndroidRawContact(events = events)
        val contact = someContactsAndroidContact(rawContacts = listOf(rawContact))

        val result = underTest.getContactData(contact)

        val eventDates = result.filterIsInstance<EventDate>()
        assertThat(eventDates).hasSize(1)
        assertThat(eventDates[0].id).isEqualTo(ContactDataIdAndroid(contactDataNo = 50L))
        assertThat(eventDates[0].type).isEqualTo(ContactDataType.Birthday)
    }

    @Test
    fun `should sanitize away event with null date`() {
        val events = listOf(someContactsAndroidEvent(id = 50L, date = null))
        val rawContact = someContactsAndroidRawContact(events = events)
        val contact = someContactsAndroidContact(rawContacts = listOf(rawContact))

        val result = underTest.getContactData(contact)

        val eventDates = result.filterIsInstance<EventDate>()
        assertThat(eventDates).isEmpty()
    }

    // ── Companies ──────────────────────────────────────────────────────────────

    @Test
    fun `should map pseudo-relation companies`() {
        val companyLabel = companyMappingService.encodeToPseudoRelationshipLabel(ContactDataType.Business)
        val relations = listOf(
            someContactsAndroidRelation(
                id = 60L,
                name = "Rebel Corp",
                type = RelationEntity.Type.CUSTOM,
                label = companyLabel,
            ),
        )
        val rawContact = someContactsAndroidRawContact(relations = relations)
        val contact = someContactsAndroidContact(rawContacts = listOf(rawContact))

        val result = underTest.getContactData(contact)

        val companies = result.filterIsInstance<Company>()
        assertThat(companies).hasSize(1)
        assertThat(companies[0].value).isEqualTo("Rebel Corp")
        assertThat(companies[0].type).isEqualTo(ContactDataType.Business)
    }

    @Test
    fun `should map organization to company`() {
        val organization = someContactsAndroidOrganization(company = "Jedi Order")
        val rawContact = someContactsAndroidRawContact(organization = organization)
        val contact = someContactsAndroidContact(rawContacts = listOf(rawContact))

        val result = underTest.getContactData(contact)

        val companies = result.filterIsInstance<Company>()
        assertThat(companies).hasSize(1)
        assertThat(companies[0].value).isEqualTo("Jedi Order")
        assertThat(companies[0].type).isEqualTo(ContactDataType.Main)
    }

    @Test
    fun `should map organization with job title to company`() {
        val organization = someContactsAndroidOrganization(company = "Jedi Order", title = "Master")
        val rawContact = someContactsAndroidRawContact(organization = organization)
        val contact = someContactsAndroidContact(rawContacts = listOf(rawContact))

        val result = underTest.getContactData(contact)

        val companies = result.filterIsInstance<Company>()
        assertThat(companies).hasSize(1)
        assertThat(companies[0].value).isEqualTo("Jedi Order (Master)")
    }

    @Test
    fun `should map organization with department to company`() {
        val organization = someContactsAndroidOrganization(company = "Jedi Order", department = "Council")
        val rawContact = someContactsAndroidRawContact(organization = organization)
        val contact = someContactsAndroidContact(rawContacts = listOf(rawContact))

        val result = underTest.getContactData(contact)

        val companies = result.filterIsInstance<Company>()
        assertThat(companies).hasSize(1)
        assertThat(companies[0].value).isEqualTo("Jedi Order - Council")
    }

    @Test
    fun `should map organization with job title and department to company`() {
        val organization = someContactsAndroidOrganization(
            company = "Jedi Order",
            title = "Master",
            department = "Council",
        )
        val rawContact = someContactsAndroidRawContact(organization = organization)
        val contact = someContactsAndroidContact(rawContacts = listOf(rawContact))

        val result = underTest.getContactData(contact)

        val companies = result.filterIsInstance<Company>()
        assertThat(companies).hasSize(1)
        assertThat(companies[0].value).isEqualTo("Jedi Order - Council (Master)")
    }

    @Test
    fun `should combine pseudo-relation companies and organization company`() {
        val companyLabel = companyMappingService.encodeToPseudoRelationshipLabel(ContactDataType.Business)
        val relations = listOf(
            someContactsAndroidRelation(
                id = 60L,
                name = "Rebel Corp",
                type = RelationEntity.Type.CUSTOM,
                label = companyLabel,
            ),
        )
        val organization = someContactsAndroidOrganization(company = "Jedi Order")
        val rawContact = someContactsAndroidRawContact(relations = relations, organization = organization)
        val contact = someContactsAndroidContact(rawContacts = listOf(rawContact))

        val result = underTest.getContactData(contact)

        val companies = result.filterIsInstance<Company>()
        assertThat(companies).hasSize(2)
        assertThat(companies.map { it.value }).containsExactlyInAnyOrder("Rebel Corp", "Jedi Order")
    }

    // ── Edge Cases ─────────────────────────────────────────────────────────────

    @Test
    fun `should return empty list when no raw contacts`() {
        val contact = someContactsAndroidContact(rawContacts = emptyList())

        val result = underTest.getContactData(contact)

        assertThat(result).isEmpty()
    }

    @Test
    fun `should set model status to UNCHANGED for all contact data`() {
        val phones = listOf(someContactsAndroidPhone())
        val emails = listOf(someContactsAndroidEmail())
        val rawContact = someContactsAndroidRawContact(phones = phones, emails = emails)
        val contact = someContactsAndroidContact(rawContacts = listOf(rawContact))

        val result = underTest.getContactData(contact)

        assertThat(result).allMatch { it.modelStatus == ModelStatus.UNCHANGED }
    }

    // ── getContactPhoneNumbers ──────────────────────────────────────────────────

    @Test
    fun `getContactPhoneNumbers should return only phone numbers`() {
        val phones = listOf(
            someContactsAndroidPhone(number = "+41791234567"),
            someContactsAndroidPhone(id = 2L, number = "+41791234568"),
        )
        val emails = listOf(someContactsAndroidEmail())
        val rawContact = someContactsAndroidRawContact(phones = phones, emails = emails)
        val contact = someContactsAndroidContact(rawContacts = listOf(rawContact))

        val result = underTest.getContactPhoneNumbers(contact)

        assertThat(result).hasSize(2)
        assertThat(result).allMatch { it is PhoneNumber }
    }

    @Test
    fun `getContactPhoneNumbers should return empty list when no raw contacts`() {
        val contact = someContactsAndroidContact(rawContacts = emptyList())

        val result = underTest.getContactPhoneNumbers(contact)

        assertThat(result).isEmpty()
    }
}
