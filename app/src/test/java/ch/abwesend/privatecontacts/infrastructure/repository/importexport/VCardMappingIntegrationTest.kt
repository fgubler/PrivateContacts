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
import ch.abwesend.privatecontacts.domain.model.result.generic.SuccessResult
import ch.abwesend.privatecontacts.domain.service.interfaces.IAddressFormattingService
import ch.abwesend.privatecontacts.domain.util.Constants
import ch.abwesend.privatecontacts.infrastructure.repository.vcard.mapping.ContactToVCardMapper
import ch.abwesend.privatecontacts.infrastructure.repository.vcard.mapping.VCardToContactMapper
import ch.abwesend.privatecontacts.infrastructure.repository.vcard.mapping.contactdata.import.ToPhysicalAddressMapper
import ch.abwesend.privatecontacts.infrastructure.service.addressformatting.AddressFormattingService
import ch.abwesend.privatecontacts.testutil.RepositoryTestBase
import ch.abwesend.privatecontacts.testutil.databuilders.someContactEditable
import ch.abwesend.privatecontacts.testutil.databuilders.someEmailAddress
import ch.abwesend.privatecontacts.testutil.databuilders.someEventDate
import ch.abwesend.privatecontacts.testutil.databuilders.somePhoneNumber
import ch.abwesend.privatecontacts.testutil.databuilders.somePhysicalAddress
import ch.abwesend.privatecontacts.testutil.databuilders.someRelationship
import ch.abwesend.privatecontacts.testutil.databuilders.someWebsite
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.core.module.Module
import java.time.LocalDate
import java.util.UUID

/**
 * These tests are based on mapping a contact to a vcard and back.
 * This is a lot more convenient than just testing one direction and also guarantees
 * that no "loss" occurs during the mappings.
 */
@ExperimentalCoroutinesApi
@ExtendWith(MockKExtension::class)
class VCardMappingIntegrationTest : RepositoryTestBase() {
    @InjectMockKs
    private lateinit var toVCardMapper: ContactToVCardMapper

    @InjectMockKs
    private lateinit var fromVCardMapper: VCardToContactMapper

    override fun setupKoinModule(module: Module) {
        super.setupKoinModule(module)
        module.single { ToPhysicalAddressMapper() }
        module.single<IAddressFormattingService> { AddressFormattingService() }
    }

    @Test
    fun `should map the UUID, names and notes`() {
        val uuid = UUID.randomUUID()
        val type = ContactType.SECRET
        val originalContact = someContactEditable(
            id = ContactIdInternal(uuid),
            type = type,
            notes = "This is a note"
        )

        val vCardResult = toVCardMapper.mapToVCard(originalContact)
        assertThat(vCardResult).isInstanceOf(SuccessResult::class.java)
        val vCard = vCardResult.getValueOrNull()
        assertThat(vCard).isNotNull

        val contactResult = fromVCardMapper.mapToContact(vCard!!, type)

        assertThat(contactResult).isInstanceOf(SuccessResult::class.java)
        val resultContact = contactResult.getValueOrNull()
        assertThat(resultContact).isNotNull
        assertThat(resultContact!!.importId?.value).isEqualTo(uuid)
        assertThat(resultContact.id).isInstanceOf(IContactIdInternal::class.java)
        assertThat((resultContact.id as IContactIdInternal).uuid).isNotEqualTo(uuid) // should choose a new UUID
        assertThat(resultContact.type).isEqualTo(type)
        assertThat(resultContact.firstName).isEqualTo(originalContact.firstName)
        assertThat(resultContact.lastName).isEqualTo(originalContact.lastName)
        assertThat(resultContact.nickname).isEqualTo(originalContact.nickname)
        assertThat(resultContact.notes).isEqualTo(originalContact.notes)
    }

    @Test
    fun `should map phone-numbers`() {
        val phoneNumbers = listOf(
            somePhoneNumber(value = "123", sortOrder = 0, type = Mobile),
            somePhoneNumber(value = "345", sortOrder = 2, type = Personal),
            somePhoneNumber(value = "234", sortOrder = 1, type = Business),
            somePhoneNumber(value = "456", sortOrder = 3, type = Other),
            somePhoneNumber(value = "789", sortOrder = 5, type = CustomValue("Test")),
            somePhoneNumber(value = "567", sortOrder = 4, type = Main),
        )
        val originalContact = someContactEditable(contactData = phoneNumbers)

        val vCardResult = toVCardMapper.mapToVCard(originalContact)
        assertThat(vCardResult).isInstanceOf(SuccessResult::class.java)
        val vCard = vCardResult.getValueOrNull()
        assertThat(vCard).isNotNull

        val contactResult = fromVCardMapper.mapToContact(vCard!!, originalContact.type)

        assertThat(contactResult).isInstanceOf(SuccessResult::class.java)
        val resultContact = contactResult.getValueOrNull()
        assertThat(resultContact).isNotNull
        assertThat(resultContact!!.contactDataSet).hasSameSizeAs(phoneNumbers)
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

    @Test
    fun `should map email-addresses`() {
        val emailAddresses = listOf(
            someEmailAddress(value = "c@d.e", sortOrder = 1, type = Personal),
            someEmailAddress(value = "b@c.d", sortOrder = 0, type = Business),
            someEmailAddress(value = "d@e.f", sortOrder = 2, type = Other),
            someEmailAddress(value = "f@g.h", sortOrder = 4, type = CustomValue("Test")),
            someEmailAddress(value = "e@f.g", sortOrder = 3, type = Main),
        )
        val originalContact = someContactEditable(contactData = emailAddresses)

        val vCardResult = toVCardMapper.mapToVCard(originalContact)
        assertThat(vCardResult).isInstanceOf(SuccessResult::class.java)
        val vCard = vCardResult.getValueOrNull()
        assertThat(vCard).isNotNull

        val contactResult = fromVCardMapper.mapToContact(vCard!!, originalContact.type)

        assertThat(contactResult).isInstanceOf(SuccessResult::class.java)
        val resultContact = contactResult.getValueOrNull()
        assertThat(resultContact).isNotNull
        assertThat(resultContact!!.contactDataSet).hasSameSizeAs(emailAddresses)
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

    @Test
    fun `should map physical addresses`() {
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

        val vCardResult = toVCardMapper.mapToVCard(originalContact)
        assertThat(vCardResult).isInstanceOf(SuccessResult::class.java)
        val vCard = vCardResult.getValueOrNull()
        assertThat(vCard).isNotNull

        val contactResult = fromVCardMapper.mapToContact(vCard!!, originalContact.type)

        assertThat(contactResult).isInstanceOf(SuccessResult::class.java)
        val resultContact = contactResult.getValueOrNull()
        assertThat(resultContact).isNotNull
        assertThat(resultContact!!.contactDataSet).hasSameSizeAs(addresses)
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

    @Test
    fun `should map websites`() {
        val websites = listOf(
            someWebsite(value = "www.google.ch", sortOrder = 1, type = Personal),
            someWebsite(value = "www.android.com", sortOrder = 0, type = Business),
            someWebsite(value = "http://mail.google.com", sortOrder = 2, type = Other),
            someWebsite(value = "https://calendar.google.com", sortOrder = 4, type = CustomValue("Test")),
            someWebsite(value = "ftp://alpha.beta.org", sortOrder = 3, type = Main),
        )
        val originalContact = someContactEditable(contactData = websites)

        val vCardResult = toVCardMapper.mapToVCard(originalContact)
        assertThat(vCardResult).isInstanceOf(SuccessResult::class.java)
        val vCard = vCardResult.getValueOrNull()
        assertThat(vCard).isNotNull

        val contactResult = fromVCardMapper.mapToContact(vCard!!, originalContact.type)

        assertThat(contactResult).isInstanceOf(SuccessResult::class.java)
        val resultContact = contactResult.getValueOrNull()
        assertThat(resultContact).isNotNull
        assertThat(resultContact!!.contactDataSet).hasSameSizeAs(websites)
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

        val vCardResult = toVCardMapper.mapToVCard(originalContact)
        assertThat(vCardResult).isInstanceOf(SuccessResult::class.java)
        val vCard = vCardResult.getValueOrNull()
        assertThat(vCard).isNotNull

        val contactResult = fromVCardMapper.mapToContact(vCard!!, originalContact.type)

        assertThat(contactResult).isInstanceOf(SuccessResult::class.java)
        val resultContact = contactResult.getValueOrNull()
        assertThat(resultContact).isNotNull
        assertThat(resultContact!!.contactDataSet).hasSameSizeAs(relationships)
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

    /**
     * Beware:
     *  - Sort-Order is lost between Anniversary and Birthday
     */
    @Test
    fun `should map event dates`() {
        val dateWithoutYear = EventDate.createDate(day = 5, month = 1, year = null)!!
        val eventDates = listOf(
            someEventDate(value = LocalDate.now(), sortOrder = 1, type = Anniversary),
            someEventDate(value = LocalDate.now().minusDays(2), sortOrder = 2, type = Anniversary),
            someEventDate(value = dateWithoutYear, sortOrder = 0, type = Birthday),
            someEventDate(value = dateWithoutYear.minusDays(3), sortOrder = 3, type = Birthday),
        )
        val originalContact = someContactEditable(contactData = eventDates)

        val vCardResult = toVCardMapper.mapToVCard(originalContact)
        assertThat(vCardResult).isInstanceOf(SuccessResult::class.java)
        val vCard = vCardResult.getValueOrNull()
        assertThat(vCard).isNotNull

        val contactResult = fromVCardMapper.mapToContact(vCard!!, originalContact.type)

        assertThat(contactResult).isInstanceOf(SuccessResult::class.java)
        val resultContact = contactResult.getValueOrNull()
        assertThat(resultContact).isNotNull
        assertThat(resultContact!!.contactDataSet).hasSameSizeAs(eventDates)
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

    // TODO add test for companies
}
