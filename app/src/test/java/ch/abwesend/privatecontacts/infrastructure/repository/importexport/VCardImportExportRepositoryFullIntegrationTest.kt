/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository.importexport

import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.ModelStatus
import ch.abwesend.privatecontacts.domain.model.contact.ContactIdInternal
import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.model.contact.IContactIdInternal
import ch.abwesend.privatecontacts.domain.model.contactdata.Company
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataCategory
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.Anniversary
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.Birthday
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.Business
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.CustomValue
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.Main
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.Mobile
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.Other
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.Personal
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.RelationshipBrother
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.RelationshipChild
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.RelationshipFriend
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.RelationshipParent
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.RelationshipPartner
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.RelationshipRelative
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.RelationshipSister
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.RelationshipWork
import ch.abwesend.privatecontacts.domain.model.contactdata.EmailAddress
import ch.abwesend.privatecontacts.domain.model.contactdata.EventDate
import ch.abwesend.privatecontacts.domain.model.contactdata.PhoneNumber
import ch.abwesend.privatecontacts.domain.model.contactdata.PhysicalAddress
import ch.abwesend.privatecontacts.domain.model.contactdata.Relationship
import ch.abwesend.privatecontacts.domain.model.contactdata.Website
import ch.abwesend.privatecontacts.domain.model.importexport.VCardVersion
import ch.abwesend.privatecontacts.domain.model.result.generic.SuccessResult
import ch.abwesend.privatecontacts.domain.service.ContactSanitizingService
import ch.abwesend.privatecontacts.domain.service.interfaces.IAddressFormattingService
import ch.abwesend.privatecontacts.domain.util.Constants
import ch.abwesend.privatecontacts.infrastructure.repository.vcard.mapping.ContactToVCardMapper
import ch.abwesend.privatecontacts.infrastructure.repository.vcard.mapping.VCardToContactMapper
import ch.abwesend.privatecontacts.infrastructure.repository.vcard.mapping.contactdata.import.ToPhysicalAddressMapper
import ch.abwesend.privatecontacts.infrastructure.repository.vcard.repository.VCardImportExportRepository
import ch.abwesend.privatecontacts.infrastructure.repository.vcard.repository.VCardRepository
import ch.abwesend.privatecontacts.infrastructure.service.AndroidContactCompanyMappingService
import ch.abwesend.privatecontacts.infrastructure.service.addressformatting.AddressFormattingService
import ch.abwesend.privatecontacts.testutil.RepositoryTestBase
import ch.abwesend.privatecontacts.testutil.databuilders.someCompany
import ch.abwesend.privatecontacts.testutil.databuilders.someContactEditable
import ch.abwesend.privatecontacts.testutil.databuilders.someContactImage
import ch.abwesend.privatecontacts.testutil.databuilders.someEmailAddress
import ch.abwesend.privatecontacts.testutil.databuilders.someEventDate
import ch.abwesend.privatecontacts.testutil.databuilders.somePhoneNumber
import ch.abwesend.privatecontacts.testutil.databuilders.somePhysicalAddress
import ch.abwesend.privatecontacts.testutil.databuilders.someRelationship
import ch.abwesend.privatecontacts.testutil.databuilders.someWebsite
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.koin.core.module.Module
import java.time.LocalDate
import java.util.UUID

/**
 * These tests are based on mapping a contact to a vcard and back, including serialization.
 * This is a lot more convenient than just testing one direction and also guarantees
 * that no "loss" occurs during the mappings.
 */
@ExperimentalCoroutinesApi
@ExtendWith(MockKExtension::class)
class VCardImportExportRepositoryFullIntegrationTest : RepositoryTestBase() {
    @InjectMockKs
    private lateinit var underTest: VCardImportExportRepository

    override fun setupKoinModule(module: Module) {
        super.setupKoinModule(module)
        module.single { ToPhysicalAddressMapper() }
        module.single { AndroidContactCompanyMappingService() }
        module.single { ContactSanitizingService() }
        module.single { VCardToContactMapper() }
        module.single { ContactToVCardMapper() }
        module.single { VCardRepository() }
        module.single<IAddressFormattingService> { AddressFormattingService() }
    }

    @ParameterizedTest
    @ValueSource(strings = ["V3", "V4"])
    fun `should map the UUID, names and notes`(vCardVersionRaw: String) {
        val vCardVersion = VCardVersion.valueOf(vCardVersionRaw)
        val uuid = UUID.randomUUID()
        val type = ContactType.SECRET
        val originalContact = someContactEditable(
            id = ContactIdInternal(uuid),
            type = type,
            notes = "This is a note"
        )

        val vCardResult = runBlocking { underTest.exportContacts(listOf(originalContact), vCardVersion) }
        assertThat(vCardResult).isInstanceOf(SuccessResult::class.java)
        val vCardContent = vCardResult.getValueOrNull()?.fileContent
        assertThat(vCardContent).isNotNull

        val contactResult = runBlocking { underTest.parseContacts(vCardContent!!, type) }
        assertThat(contactResult).isInstanceOf(SuccessResult::class.java)
        val resultContacts = contactResult.getValueOrNull()?.successfulContacts
        assertThat(resultContacts).isNotNull.isNotEmpty.hasSize(1)
        val resultContact = resultContacts!!.first()

        assertThat(resultContact.importId?.uuid).isEqualTo(uuid)
        assertThat(resultContact.id).isInstanceOf(IContactIdInternal::class.java)
        assertThat((resultContact.id as IContactIdInternal).uuid).isNotEqualTo(uuid) // should choose a new UUID
        assertThat(resultContact.type).isEqualTo(type)
        assertThat(resultContact.firstName).isEqualTo(originalContact.firstName)
        assertThat(resultContact.lastName).isEqualTo(originalContact.lastName)
        assertThat(resultContact.nickname).isEqualTo(originalContact.nickname)
        assertThat(resultContact.notes).isEqualTo(originalContact.notes)
    }

    @ParameterizedTest
    @ValueSource(strings = ["V3", "V4"])
    fun `should map phone-numbers`(vCardVersionRaw: String) {
        val vCardVersion = VCardVersion.valueOf(vCardVersionRaw)
        val phoneNumbers = listOf(
            somePhoneNumber(value = "123", sortOrder = 0, type = Mobile),
            somePhoneNumber(value = "345", sortOrder = 2, type = Personal),
            somePhoneNumber(value = "234", sortOrder = 1, type = Business),
            somePhoneNumber(value = "456", sortOrder = 3, type = Other),
            somePhoneNumber(value = "789", sortOrder = 5, type = CustomValue("Test")),
            somePhoneNumber(value = "567", sortOrder = 4, type = Main),
        )
        val originalContact = someContactEditable(contactData = phoneNumbers)

        val vCardResult = runBlocking { underTest.exportContacts(listOf(originalContact), vCardVersion) }
        assertThat(vCardResult).isInstanceOf(SuccessResult::class.java)
        val vCardContent = vCardResult.getValueOrNull()?.fileContent
        assertThat(vCardContent).isNotNull

        val contactResult = runBlocking { underTest.parseContacts(vCardContent!!, originalContact.type) }
        assertThat(contactResult).isInstanceOf(SuccessResult::class.java)
        val resultContacts = contactResult.getValueOrNull()?.successfulContacts
        assertThat(resultContacts).isNotNull.isNotEmpty.hasSize(1)
        val resultContact = resultContacts!!.first()

        assertThat(resultContact.contactDataSet).hasSameSizeAs(phoneNumbers)
        val resultPhoneNumbers = resultContact.contactDataSet
        val sortedOriginalPhoneNumbers = phoneNumbers.sortedBy { it.sortOrder }
        resultPhoneNumbers.indices.forEach { index ->
            val resultPhoneNumber = resultPhoneNumbers[index]
            val originalPhoneNumber = sortedOriginalPhoneNumbers[index]
            logger.debug("testing phone-number $originalPhoneNumber")
            assertThat(resultPhoneNumber).isInstanceOf(PhoneNumber::class.java)
            assertThat(resultPhoneNumber.category).isEqualTo(ContactDataCategory.PHONE_NUMBER)
            assertThat(resultPhoneNumber.sortOrder).isEqualTo(originalPhoneNumber.sortOrder)
            assertThat(resultPhoneNumber.value).isEqualTo(originalPhoneNumber.value)
            assertThat(resultPhoneNumber.modelStatus).isEqualTo(ModelStatus.NEW)
            assertThat(resultPhoneNumber.type).isEqualTo(originalPhoneNumber.type)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["V3", "V4"])
    fun `should map email-addresses`(vCardVersionRaw: String) {
        val vCardVersion = VCardVersion.valueOf(vCardVersionRaw)
        val emailAddresses = listOf(
            someEmailAddress(value = "c@d.e", sortOrder = 1, type = Personal),
            someEmailAddress(value = "b@c.d", sortOrder = 0, type = Business),
            someEmailAddress(value = "d@e.f", sortOrder = 2, type = Other),
            someEmailAddress(value = "f@g.h", sortOrder = 4, type = CustomValue("Test")),
            someEmailAddress(value = "e@f.g", sortOrder = 3, type = Main),
        )
        val originalContact = someContactEditable(contactData = emailAddresses)

        val vCardResult = runBlocking { underTest.exportContacts(listOf(originalContact), vCardVersion) }
        assertThat(vCardResult).isInstanceOf(SuccessResult::class.java)
        val vCardContent = vCardResult.getValueOrNull()?.fileContent
        assertThat(vCardContent).isNotNull

        val contactResult = runBlocking { underTest.parseContacts(vCardContent!!, originalContact.type) }
        assertThat(contactResult).isInstanceOf(SuccessResult::class.java)
        val resultContacts = contactResult.getValueOrNull()?.successfulContacts
        assertThat(resultContacts).isNotNull.isNotEmpty.hasSize(1)
        val resultContact = resultContacts!!.first()

        assertThat(resultContact.contactDataSet).hasSameSizeAs(emailAddresses)
        val resultEmailAddresses = resultContact.contactDataSet
        val sortedOriginalEmailAddresses = emailAddresses.sortedBy { it.sortOrder }
        resultEmailAddresses.indices.forEach { index ->
            val resultEmailAddress = resultEmailAddresses[index]
            val originalEmailAddress = sortedOriginalEmailAddresses[index]
            logger.debug("testing email-address $originalEmailAddress")
            assertThat(resultEmailAddress).isInstanceOf(EmailAddress::class.java)
            assertThat(resultEmailAddress.category).isEqualTo(ContactDataCategory.EMAIL)
            assertThat(resultEmailAddress.sortOrder).isEqualTo(originalEmailAddress.sortOrder)
            assertThat(resultEmailAddress.value).isEqualTo(originalEmailAddress.value)
            assertThat(resultEmailAddress.modelStatus).isEqualTo(ModelStatus.NEW)
            assertThat(resultEmailAddress.type).isEqualTo(originalEmailAddress.type)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["V3", "V4"])
    fun `should map physical addresses`(vCardVersionRaw: String) {
        val vCardVersion = VCardVersion.valueOf(vCardVersionRaw)
        val linebreak = Constants.linebreak
        val longAddress = "123 some long street, $linebreak some town, $linebreak some state, $linebreak USA"
        val addresses = listOf(
            somePhysicalAddress(value = "alphastrasse 155, 8000 Zürich", sortOrder = 1, type = Personal),
            somePhysicalAddress(value = "123 broadway $linebreak NYC $linebreak NY", sortOrder = 0, type = Business),
            somePhysicalAddress(value = longAddress, sortOrder = 2, type = Other),
            somePhysicalAddress(value = "customStreet 1", sortOrder = 4, type = CustomValue("Test")),
            somePhysicalAddress(value = "mainstreet 4", sortOrder = 3, type = Main),
        )
        val originalContact = someContactEditable(contactData = addresses)

        val vCardResult = runBlocking { underTest.exportContacts(listOf(originalContact), vCardVersion) }
        assertThat(vCardResult).isInstanceOf(SuccessResult::class.java)
        val vCardContent = vCardResult.getValueOrNull()?.fileContent
        assertThat(vCardContent).isNotNull

        val contactResult = runBlocking { underTest.parseContacts(vCardContent!!, originalContact.type) }
        assertThat(contactResult).isInstanceOf(SuccessResult::class.java)
        val resultContacts = contactResult.getValueOrNull()?.successfulContacts
        assertThat(resultContacts).isNotNull.isNotEmpty.hasSize(1)
        val resultContact = resultContacts!!.first()

        assertThat(resultContact.contactDataSet).hasSameSizeAs(addresses)
        val resultAddresses = resultContact.contactDataSet
        val sortedOriginalAddresses = addresses.sortedBy { it.sortOrder }
        resultAddresses.indices.forEach { index ->
            val resultAddress = resultAddresses[index]
            val originalAddress = sortedOriginalAddresses[index]
            logger.debug("testing address $originalAddress")
            assertThat(resultAddress).isInstanceOf(PhysicalAddress::class.java)
            assertThat(resultAddress.category).isEqualTo(ContactDataCategory.ADDRESS)
            assertThat(resultAddress.sortOrder).isEqualTo(originalAddress.sortOrder)
            val resultValue = (resultAddress.value as String).replace(",", "").replace(" ", "")
            val originalValue = originalAddress.value.replace(",", " ").replace(" ", "")
            assertThat(resultValue).isEqualTo(originalValue)
            assertThat(resultAddress.modelStatus).isEqualTo(ModelStatus.NEW)
            assertThat(resultAddress.type).isEqualTo(originalAddress.type)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["V3", "V4"])
    fun `should map websites`(vCardVersionRaw: String) {
        val vCardVersion = VCardVersion.valueOf(vCardVersionRaw)
        val websites = listOf(
            someWebsite(value = "www.google.ch", sortOrder = 1, type = Personal),
            someWebsite(value = "www.android.com", sortOrder = 0, type = Business),
            someWebsite(value = "http://mail.google.com", sortOrder = 2, type = Other),
            someWebsite(value = "https://calendar.google.com", sortOrder = 4, type = CustomValue("Test")),
            someWebsite(value = "ftp://alpha.beta.org", sortOrder = 3, type = Main),
        )
        val originalContact = someContactEditable(contactData = websites)

        val vCardResult = runBlocking { underTest.exportContacts(listOf(originalContact), vCardVersion) }
        assertThat(vCardResult).isInstanceOf(SuccessResult::class.java)
        val vCardContent = vCardResult.getValueOrNull()?.fileContent
        assertThat(vCardContent).isNotNull

        val contactResult = runBlocking { underTest.parseContacts(vCardContent!!, originalContact.type) }
        assertThat(contactResult).isInstanceOf(SuccessResult::class.java)
        val resultContacts = contactResult.getValueOrNull()?.successfulContacts
        assertThat(resultContacts).isNotNull.isNotEmpty.hasSize(1)
        val resultContact = resultContacts!!.first()

        assertThat(resultContact.contactDataSet).hasSameSizeAs(websites)
        val resultWebsites = resultContact.contactDataSet
        val sortedOriginalWebsites = websites.sortedBy { it.sortOrder }
        resultWebsites.indices.forEach { index ->
            val resultWebsite = resultWebsites[index]
            val originalWebsite = sortedOriginalWebsites[index]
            logger.debug("testing website $originalWebsite")
            assertThat(resultWebsite).isInstanceOf(Website::class.java)
            assertThat(resultWebsite.category).isEqualTo(ContactDataCategory.WEBSITE)
            assertThat(resultWebsite.sortOrder).isEqualTo(originalWebsite.sortOrder)
            assertThat(resultWebsite.value).isEqualTo(originalWebsite.value)
            assertThat(resultWebsite.modelStatus).isEqualTo(ModelStatus.NEW)
            assertThat(resultWebsite.type).isEqualTo(originalWebsite.type)
        }
    }

    @Test
    fun `should map relationships`() {
        val vCardVersion = VCardVersion.V4 // V3 does not support relationships
        val relationships = listOf(
            someRelationship(value = "Dorothee", sortOrder = 1, type = RelationshipChild),
            someRelationship(value = "Karen", sortOrder = 0, type = RelationshipSister),
            someRelationship(value = "Sebastian", sortOrder = 3, type = RelationshipBrother),
            someRelationship(value = "Anne", sortOrder = 5, type = RelationshipFriend),
            someRelationship(value = "Kathrin", sortOrder = 6, type = RelationshipPartner),
            someRelationship(value = "Tom", sortOrder = 8, type = RelationshipRelative),
            someRelationship(value = "Marc", sortOrder = 7, type = RelationshipParent),
            someRelationship(value = "Tobias", sortOrder = 9, type = RelationshipWork),
            someRelationship(value = "Alex", sortOrder = 2, type = Other),
            someRelationship(value = "Pascal", sortOrder = 4, type = CustomValue("Test")),
        )
        val originalContact = someContactEditable(contactData = relationships)

        val vCardResult = runBlocking { underTest.exportContacts(listOf(originalContact), vCardVersion) }
        assertThat(vCardResult).isInstanceOf(SuccessResult::class.java)
        val vCardContent = vCardResult.getValueOrNull()?.fileContent
        assertThat(vCardContent).isNotNull

        val contactResult = runBlocking { underTest.parseContacts(vCardContent!!, originalContact.type) }
        assertThat(contactResult).isInstanceOf(SuccessResult::class.java)
        val resultContacts = contactResult.getValueOrNull()?.successfulContacts
        assertThat(resultContacts).isNotNull.isNotEmpty.hasSize(1)
        val resultContact = resultContacts!!.first()

        assertThat(resultContact.contactDataSet).hasSameSizeAs(relationships)
        val resultRelationships = resultContact.contactDataSet
        val sortedOriginalRelationships = relationships.sortedBy { it.sortOrder }
        resultRelationships.indices.forEach { index ->
            val resultRelationship = resultRelationships[index]
            val originalRelationship = sortedOriginalRelationships[index]
            logger.debug("testing relationship $originalRelationship")
            assertThat(resultRelationship).isInstanceOf(Relationship::class.java)
            assertThat(resultRelationship.category).isEqualTo(ContactDataCategory.RELATIONSHIP)
            assertThat(resultRelationship.sortOrder).isEqualTo(originalRelationship.sortOrder)
            assertThat(resultRelationship.value).isEqualTo(originalRelationship.value)
            assertThat(resultRelationship.modelStatus).isEqualTo(ModelStatus.NEW)
            assertThat(resultRelationship.type).isEqualTo(originalRelationship.type)
        }
    }

    @Test
    fun `should map companies`() {
        val vCardVersion = VCardVersion.V4 // V3 does not support relationships or organizations
        val companies = listOf(
            someCompany(value = "Google", sortOrder = 1, type = CustomValue("PhoneProvider")),
            someCompany(value = "Apple", sortOrder = 0, type = Other),
            someCompany(value = "Amazon", sortOrder = 2, type = CustomValue("Shopping")),
            someCompany(value = "Ergon Informatik", sortOrder = 3, type = Main),
        )
        val originalContact = someContactEditable(contactData = companies)

        val vCardResult = runBlocking { underTest.exportContacts(listOf(originalContact), vCardVersion) }
        assertThat(vCardResult).isInstanceOf(SuccessResult::class.java)
        val vCardContent = vCardResult.getValueOrNull()?.fileContent
        assertThat(vCardContent).isNotNull

        val contactResult = runBlocking { underTest.parseContacts(vCardContent!!, originalContact.type) }
        assertThat(contactResult).isInstanceOf(SuccessResult::class.java)
        val resultContacts = contactResult.getValueOrNull()?.successfulContacts
        assertThat(resultContacts).isNotNull.isNotEmpty.hasSize(1)
        val resultContact = resultContacts!!.first()

        assertThat(resultContact.contactDataSet).hasSameSizeAs(companies)
        val resultCompanies = resultContact.contactDataSet
        val sortedOriginalCompanies = companies.sortedBy { it.sortOrder }
        resultCompanies.indices.forEach { index ->
            val resultCompany = resultCompanies[index]
            val originalCompany = sortedOriginalCompanies[index]
            logger.debug("testing company $originalCompany")
            assertThat(resultCompany).isInstanceOf(Company::class.java)
            assertThat(resultCompany.category).isEqualTo(ContactDataCategory.COMPANY)
            assertThat(resultCompany.sortOrder).isEqualTo(originalCompany.sortOrder)
            assertThat(resultCompany.modelStatus).isEqualTo(ModelStatus.NEW)
            assertThat(resultCompany.value).isEqualTo(originalCompany.value)

            // for some reason, the type seems to be changed to all-lower-case in VCF...
            val resultType = resultCompany.type
            if (resultType is CustomValue) {
                val originalType = originalCompany.type
                assertThat(originalType).isInstanceOf(CustomValue::class.java)
                val originalCustomValue = (originalCompany.type as CustomValue).customValue.lowercase()
                assertThat(resultType.customValue.lowercase()).isEqualTo(originalCustomValue)
            } else {
                assertThat(resultCompany.type).isEqualTo(originalCompany.type)
            }
        }
    }

    /** to make sure there are no conflicts between these two types */
    @Test
    fun `should map relationships and companies together`() {
        val vCardVersion = VCardVersion.V4 // V3 does not support relationships or organizations
        val relationships = listOf(
            someRelationship(value = "Dorothee", sortOrder = 1, type = RelationshipChild),
            someRelationship(value = "Sebastian", sortOrder = 0, type = RelationshipBrother),
        )
        val companies = listOf(
            someCompany(value = "Google", sortOrder = 0, type = Other),
            someCompany(value = "Ergon Informatik", sortOrder = 1, type = Main),
        )
        val originalContactData = companies + relationships
        val originalContact = someContactEditable(contactData = originalContactData)

        val vCardResult = runBlocking { underTest.exportContacts(listOf(originalContact), vCardVersion) }
        assertThat(vCardResult).isInstanceOf(SuccessResult::class.java)
        val vCardContent = vCardResult.getValueOrNull()?.fileContent
        assertThat(vCardContent).isNotNull

        val contactResult = runBlocking { underTest.parseContacts(vCardContent!!, originalContact.type) }
        assertThat(contactResult).isInstanceOf(SuccessResult::class.java)
        val resultContacts = contactResult.getValueOrNull()?.successfulContacts
        assertThat(resultContacts).isNotNull.isNotEmpty.hasSize(1)
        val resultContact = resultContacts!!.first()

        assertThat(resultContact.contactDataSet).hasSameSizeAs(originalContactData)
        val resultContactData = resultContact.contactDataSet
        val resultCompanies = resultContactData.filterIsInstance<Company>()
        val resultRelationships = resultContactData.filterIsInstance<Relationship>()
        val sortedOriginalRelationships = relationships.sortedBy { it.sortOrder }
        val sortedOriginalCompanies = companies.sortedBy { it.sortOrder }

        resultRelationships.indices.forEach { index ->
            val resultRelationship = resultRelationships[index]
            val originalRelationship = sortedOriginalRelationships[index]
            logger.debug("testing relationship $originalRelationship")
            assertThat(resultRelationship).isInstanceOf(Relationship::class.java)
            assertThat(resultRelationship.category).isEqualTo(ContactDataCategory.RELATIONSHIP)
            assertThat(resultRelationship.sortOrder).isEqualTo(originalRelationship.sortOrder)
            assertThat(resultRelationship.value).isEqualTo(originalRelationship.value)
            assertThat(resultRelationship.modelStatus).isEqualTo(ModelStatus.NEW)
            assertThat(resultRelationship.type).isEqualTo(originalRelationship.type)
        }

        resultCompanies.indices.forEach { index ->
            val resultCompany = resultCompanies[index]
            val originalCompany = sortedOriginalCompanies[index]
            logger.debug("testing company $originalCompany")
            assertThat(resultCompany).isInstanceOf(Company::class.java)
            assertThat(resultCompany.category).isEqualTo(ContactDataCategory.COMPANY)
            assertThat(resultCompany.sortOrder).isEqualTo(originalCompany.sortOrder)
            assertThat(resultCompany.modelStatus).isEqualTo(ModelStatus.NEW)
            assertThat(resultCompany.value).isEqualTo(originalCompany.value)
            assertThat(resultCompany.type).isEqualTo(originalCompany.type)
        }
    }

    /**
     * Beware:
     *  - Sort-Order is lost between Anniversary and Birthday
     */
    @Test
    fun `should map event dates`() {
        val vCardVersion = VCardVersion.V4 // V3 does not support anniversaries
        val dateWithoutYear = EventDate.createDate(day = 5, month = 1, year = null)!!
        val eventDates = listOf(
            someEventDate(value = LocalDate.now(), sortOrder = 1, type = Anniversary),
            someEventDate(value = LocalDate.now().minusDays(2), sortOrder = 2, type = Anniversary),
            someEventDate(value = dateWithoutYear, sortOrder = 0, type = Birthday),
            someEventDate(value = dateWithoutYear.minusDays(3), sortOrder = 3, type = Birthday),
        )
        val originalContact = someContactEditable(contactData = eventDates)

        val vCardResult = runBlocking { underTest.exportContacts(listOf(originalContact), vCardVersion) }
        assertThat(vCardResult).isInstanceOf(SuccessResult::class.java)
        val vCardContent = vCardResult.getValueOrNull()?.fileContent
        assertThat(vCardContent).isNotNull

        val contactResult = runBlocking { underTest.parseContacts(vCardContent!!, originalContact.type) }
        assertThat(contactResult).isInstanceOf(SuccessResult::class.java)
        val resultContacts = contactResult.getValueOrNull()?.successfulContacts
        assertThat(resultContacts).isNotNull.isNotEmpty.hasSize(1)
        val resultContact = resultContacts!!.first()

        assertThat(resultContact.contactDataSet).hasSameSizeAs(eventDates)
        val resultEventDates = resultContact.contactDataSet
        val sortedOriginalEventDates = eventDates.sortedBy { it.sortOrder }
        resultEventDates.forEach { resultEventDate ->
            // cannot really guarantee that the sort-order is being kept
            val originalEventDate = sortedOriginalEventDates.firstOrNull { it.value == resultEventDate.value }
            logger.debug("testing event-date $resultEventDate")
            assertThat(resultEventDate).isInstanceOf(EventDate::class.java)
            assertThat(resultEventDate.category).isEqualTo(ContactDataCategory.EVENT_DATE)
            assertThat(originalEventDate).isNotNull
            assertThat(resultEventDate.value).isEqualTo(originalEventDate!!.value)
            assertThat(resultEventDate.modelStatus).isEqualTo(ModelStatus.NEW)
            assertThat(resultEventDate.type).isEqualTo(originalEventDate.type)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["V3", "V4"])
    fun `should map images`(vCardVersionRaw: String) {
        val vCardVersion = VCardVersion.valueOf(vCardVersionRaw)
        val images = listOf(
            someContactImage(thumbnailUri = "Test", fullImage = byteArrayOf(0, 3, 5, 7)),
            someContactImage(thumbnailUri = null, fullImage = byteArrayOf(0, 3, 5, 7)),
            someContactImage(thumbnailUri = "Test", fullImage = null),
        )
        val originalContacts = images.map { someContactEditable(image = it) }

        images.indices.forEach {
            val image = images[it]
            val originalContact = originalContacts[it]

            val vCardResult = runBlocking { underTest.exportContacts(listOf(originalContact), vCardVersion) }
            assertThat(vCardResult).isInstanceOf(SuccessResult::class.java)
            val vCardContent = vCardResult.getValueOrNull()?.fileContent
            assertThat(vCardContent).isNotNull

            val contactResult = runBlocking { underTest.parseContacts(vCardContent!!, originalContact.type) }
            assertThat(contactResult).isInstanceOf(SuccessResult::class.java)
            val resultContacts = contactResult.getValueOrNull()?.successfulContacts
            assertThat(resultContacts).isNotNull.isNotEmpty.hasSize(1)
            val resultContact = resultContacts!!.firstOrNull()
            assertThat(resultContact).isNotNull

            assertThat(resultContact!!.image.thumbnailUri).isEqualTo(image.thumbnailUri)
            assertThat(resultContact.image.fullImage).isEqualTo(image.fullImage)
        }
    }
}
