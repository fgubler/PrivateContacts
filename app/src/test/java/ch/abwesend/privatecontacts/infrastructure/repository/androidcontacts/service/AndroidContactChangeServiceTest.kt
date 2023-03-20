/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.service

import ch.abwesend.privatecontacts.domain.model.ModelStatus
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactData
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType
import ch.abwesend.privatecontacts.domain.model.contactdata.EmailAddress
import ch.abwesend.privatecontacts.domain.model.contactdata.EventDate
import ch.abwesend.privatecontacts.domain.model.contactdata.PhoneNumber
import ch.abwesend.privatecontacts.domain.model.contactdata.PhysicalAddress
import ch.abwesend.privatecontacts.domain.model.contactdata.Relationship
import ch.abwesend.privatecontacts.domain.model.contactdata.Website
import ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.factory.IAndroidContactMutableFactory
import ch.abwesend.privatecontacts.testutil.TestBase
import ch.abwesend.privatecontacts.testutil.androidcontacts.TestAndroidContactMutableFactory
import ch.abwesend.privatecontacts.testutil.databuilders.ContactDataContainer
import ch.abwesend.privatecontacts.testutil.databuilders.someAndroidContactMutable
import ch.abwesend.privatecontacts.testutil.databuilders.someCompany
import ch.abwesend.privatecontacts.testutil.databuilders.someContactDataIdExternal
import ch.abwesend.privatecontacts.testutil.databuilders.someContactEditable
import ch.abwesend.privatecontacts.testutil.databuilders.someContactImage
import ch.abwesend.privatecontacts.testutil.mockUriParse
import com.alexstyl.contactstore.ImageData
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.SpyK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.koin.core.module.Module
import java.time.LocalDate

@ExperimentalCoroutinesApi
@ExtendWith(MockKExtension::class)
class AndroidContactChangeServiceTest : TestBase() {
    @SpyK
    private var mutableContactFactory: IAndroidContactMutableFactory = TestAndroidContactMutableFactory()

    @InjectMockKs
    private lateinit var underTest: AndroidContactChangeService

    override fun setupKoinModule(module: Module) {
        super.setupKoinModule(module)
        module.single { mutableContactFactory }
    }

    @Test
    fun `should set base-data on mutable contact`() {
        val originalContact = someContactEditable(
            firstName = "original first",
            lastName = "original last",
            nickname = "original nick",
            notes = "original notes",
        )
        val changedContact = someContactEditable(
            firstName = "changed first",
            lastName = "changed last",
            nickname = "changed nick",
            notes = "changed notes",
        )
        val mutableContact = someAndroidContactMutable()

        underTest.updateChangedBaseData(
            originalContact = originalContact,
            changedContact = changedContact,
            mutableContact = mutableContact,
        )

        assertThat(mutableContact.firstName).isEqualTo(changedContact.firstName)
        assertThat(mutableContact.lastName).isEqualTo(changedContact.lastName)
        assertThat(mutableContact.nickname).isEqualTo(changedContact.nickname)
        assertThat(mutableContact.note?.raw).isEqualTo(changedContact.notes)
    }

    @Test
    fun `should set base-data on mutable contact only for changed fields`() {
        val androidFirstName = "android first"
        val originalContact = someContactEditable(
            firstName = "original first",
            lastName = "original last",
            nickname = "original nick",
            notes = "",
        )
        val changedContact = someContactEditable(
            firstName = originalContact.firstName,
            lastName = "changed last",
            nickname = "",
            notes = "changed notes",
        )
        val mutableContact = someAndroidContactMutable(
            firstName = androidFirstName,
            lastName = "android last",
            nickName = "android nick",
            note = "android notes",
        )

        underTest.updateChangedBaseData(
            originalContact = originalContact,
            changedContact = changedContact,
            mutableContact = mutableContact,
        )

        assertThat(mutableContact.firstName).isEqualTo(androidFirstName)
        assertThat(mutableContact.lastName).isEqualTo(changedContact.lastName)
        assertThat(mutableContact.nickname).isEqualTo(changedContact.nickname)
        assertThat(mutableContact.note?.raw).isEqualTo(changedContact.notes)
    }

    @Test
    fun `should leave unchanged image as-is`() {
        val image = someContactImage(fullImage = ByteArray(size = 42), modelStatus = ModelStatus.UNCHANGED)
        val changedContact = someContactEditable(
            image = image
        )
        val mutableContact = someAndroidContactMutable(imageData = ImageData(ByteArray(size = 0)))

        underTest.updateChangedImage(changedContact = changedContact, mutableContact = mutableContact)

        assertThat(mutableContact.imageData?.raw).isNotNull.hasSize(0)
    }

    @Test
    fun `should remove deleted image`() {
        val image = someContactImage(fullImage = ByteArray(size = 42), modelStatus = ModelStatus.DELETED)
        val changedContact = someContactEditable(
            image = image
        )
        val mutableContact = someAndroidContactMutable(imageData = ImageData(ByteArray(size = 0)))

        underTest.updateChangedImage(changedContact = changedContact, mutableContact = mutableContact)

        assertThat(mutableContact.imageData).isNull()
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `should set added or changed image`(statusNew: Boolean) {
        val modelStatus = if (statusNew) ModelStatus.NEW else ModelStatus.CHANGED
        val image = someContactImage(fullImage = ByteArray(size = 42), modelStatus = modelStatus)
        val changedContact = someContactEditable(
            image = image
        )
        val mutableContact = someAndroidContactMutable(imageData = ImageData(ByteArray(size = 0)))

        underTest.updateChangedImage(changedContact = changedContact, mutableContact = mutableContact)

        assertThat(mutableContact.imageData?.raw).isNotNull.hasSize(42)
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `should add new contact data (or update if it already exists)`(dataAlreadyExists: Boolean) {
        val oldData = if (dataAlreadyExists) createContactBaseData() else ContactDataContainer.createEmpty()
        val newData = createContactBaseData(variant = true)
        val mutableContact = someAndroidContactMutable(contactData = oldData)
        val changedContact = someContactEditable(
            contactData = prepareContactDataForInternalContact(data = newData, modelStatus = ModelStatus.NEW)
        )
        mockUriParse()

        underTest.updateChangedContactData(changedContact = changedContact, mutableContact = mutableContact)

        assertThat(mutableContact.phones.map { it.value.raw }).isNotEmpty.isEqualTo(newData.phoneNumbers)
        assertThat(mutableContact.mails.map { it.value.raw }).isNotEmpty.isEqualTo(newData.emailAddresses)
        assertThat(mutableContact.postalAddresses.map { it.value.street }).isNotEmpty.isEqualTo(newData.physicalAddresses)
        assertThat(mutableContact.webAddresses.map { it.value.raw.toString() }).isNotEmpty.isEqualTo(newData.websites)
        assertThat(mutableContact.relations.map { it.value.name }).isNotEmpty.isEqualTo(newData.relationships)
        assertThat(
            mutableContact.events.map { event ->
                event.value.let { LocalDate.of(it.year!!, it.month, it.dayOfMonth) }
            }
        ).isNotEmpty.isEqualTo(newData.eventDates)
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `should update changed contact data (or insert it if it does not yet exist)`(dataAlreadyExists: Boolean) {
        val oldData = if (dataAlreadyExists) createContactBaseData() else ContactDataContainer.createEmpty()
        val newData = createContactBaseData(variant = true)
        val mutableContact = someAndroidContactMutable(contactData = oldData)
        val changedContact = someContactEditable(
            contactData = prepareContactDataForInternalContact(data = newData, modelStatus = ModelStatus.CHANGED)
        )
        mockUriParse()

        underTest.updateChangedContactData(changedContact = changedContact, mutableContact = mutableContact)

        assertThat(mutableContact.phones.map { it.value.raw }).isNotEmpty.isEqualTo(newData.phoneNumbers)
        assertThat(mutableContact.mails.map { it.value.raw }).isNotEmpty.isEqualTo(newData.emailAddresses)
        assertThat(mutableContact.postalAddresses.map { it.value.street }).isNotEmpty.isEqualTo(newData.physicalAddresses)
        assertThat(mutableContact.webAddresses.map { it.value.raw.toString() }).isNotEmpty.isEqualTo(newData.websites)
        assertThat(mutableContact.relations.map { it.value.name }).isNotEmpty.isEqualTo(newData.relationships)
        assertThat(
            mutableContact.events.map { event ->
                event.value.let { LocalDate.of(it.year!!, it.month, it.dayOfMonth) }
            }
        ).isNotEmpty.isEqualTo(newData.eventDates)
    }

    @Test
    fun `should translate main-company to the organization-field`() {
        val newMainCompanyName = "Main Inc."
        val newCompanies = listOf(
            someCompany(value = "Side-Show Inc.", type = ContactDataType.Other, modelStatus = ModelStatus.CHANGED),
            someCompany(value = newMainCompanyName, type = ContactDataType.Main, modelStatus = ModelStatus.CHANGED),
            someCompany(value = "Other Side-Show Inc.", type = ContactDataType.Other, modelStatus = ModelStatus.NEW),
        )
        val mutableContact = someAndroidContactMutable(organisation = "Old Inc.")
        val changedContact = someContactEditable(contactData = newCompanies)
        mockUriParse()

        underTest.updateChangedContactData(changedContact = changedContact, mutableContact = mutableContact)

        assertThat(mutableContact.organization).isEqualTo(newMainCompanyName)
    }

    @Test
    fun `should not change if main-company is unchanged - even if there is a changed non-main`() {
        val oldCompanyName = "Old Inc."
        val newMainCompanyName = "Main Inc."
        val newCompanies = listOf(
            someCompany(value = "Side-Show Inc.", type = ContactDataType.Other, modelStatus = ModelStatus.CHANGED),
            someCompany(value = newMainCompanyName, type = ContactDataType.Main, modelStatus = ModelStatus.UNCHANGED),
        )
        val mutableContact = someAndroidContactMutable(organisation = oldCompanyName)
        val changedContact = someContactEditable(contactData = newCompanies)
        mockUriParse()

        underTest.updateChangedContactData(changedContact = changedContact, mutableContact = mutableContact)

        assertThat(mutableContact.organization).isEqualTo(oldCompanyName)
    }

    @Test
    fun `should treat types main and business the same way`() {
        val businessName = "Business Inc."
        val newCompanies = listOf(
            someCompany(value = businessName, type = ContactDataType.Business, modelStatus = ModelStatus.CHANGED),
            someCompany(value = "Main Inc.", type = ContactDataType.Main, modelStatus = ModelStatus.CHANGED),
        )
        val mutableContact = someAndroidContactMutable(organisation = "Old Inc.")
        val changedContact = someContactEditable(contactData = newCompanies)
        mockUriParse()

        underTest.updateChangedContactData(changedContact = changedContact, mutableContact = mutableContact)

        assertThat(mutableContact.organization).isEqualTo(businessName) // because it is first in the list
    }

    @Test
    fun `should prefer changed main-company over unchanged`() {
        val newMainCompanyName = "Main Inc."
        val newCompanies = listOf(
            someCompany(value = "Other Main Inc.", type = ContactDataType.Main, modelStatus = ModelStatus.UNCHANGED),
            someCompany(value = newMainCompanyName, type = ContactDataType.Main, modelStatus = ModelStatus.CHANGED),
        )
        val mutableContact = someAndroidContactMutable(organisation = "Old Inc.")
        val changedContact = someContactEditable(contactData = newCompanies)
        mockUriParse()

        underTest.updateChangedContactData(changedContact = changedContact, mutableContact = mutableContact)

        assertThat(mutableContact.organization).isEqualTo(newMainCompanyName)
    }

    @Test
    fun `should use first changed non-main company if no main is present`() {
        val expectedCompanyName = "Changed Other"
        val newCompanies = listOf(
            someCompany(value = "Unchanged Other", type = ContactDataType.Other, modelStatus = ModelStatus.UNCHANGED),
            someCompany(value = expectedCompanyName, type = ContactDataType.Other, modelStatus = ModelStatus.CHANGED),
        )
        val mutableContact = someAndroidContactMutable(organisation = "Old Inc.")
        val changedContact = someContactEditable(contactData = newCompanies)
        mockUriParse()

        underTest.updateChangedContactData(changedContact = changedContact, mutableContact = mutableContact)

        assertThat(mutableContact.organization).isEqualTo(expectedCompanyName)
    }

    @Test
    fun `should not change anything if no changes were made`() {
        val oldCompanyName = "Old Inc."
        val newCompanies = listOf(
            someCompany(value = "Unchanged 1", modelStatus = ModelStatus.UNCHANGED),
            someCompany(value = "Unchanged 2", modelStatus = ModelStatus.UNCHANGED),
        )
        val mutableContact = someAndroidContactMutable(organisation = oldCompanyName)
        val changedContact = someContactEditable(contactData = newCompanies)
        mockUriParse()

        underTest.updateChangedContactData(changedContact = changedContact, mutableContact = mutableContact)

        assertThat(mutableContact.organization).isEqualTo(oldCompanyName)
    }

    @Test
    fun `should leave unchanged contact data as-is`() {
        val data = createContactBaseData()
        val mutableContact = someAndroidContactMutable(contactData = ContactDataContainer.createEmpty())
        val changedContact = someContactEditable(
            contactData = prepareContactDataForInternalContact(data = data, modelStatus = ModelStatus.UNCHANGED)
        )

        underTest.updateChangedContactData(changedContact = changedContact, mutableContact = mutableContact)

        assertThat(mutableContact.phones).isEmpty()
        assertThat(mutableContact.mails).isEmpty()
        assertThat(mutableContact.postalAddresses).isEmpty()
        assertThat(mutableContact.webAddresses).isEmpty()
        assertThat(mutableContact.relations).isEmpty()
        assertThat(mutableContact.events).isEmpty()
    }

    @Test
    fun `should remove deleted contact data`() {
        val data = createContactBaseData()
        val mutableContact = someAndroidContactMutable(contactData = data)
        val changedContact = someContactEditable(
            contactData = prepareContactDataForInternalContact(data = data, modelStatus = ModelStatus.DELETED)
        )

        underTest.updateChangedContactData(changedContact = changedContact, mutableContact = mutableContact)

        assertThat(mutableContact.phones).isEmpty()
        assertThat(mutableContact.mails).isEmpty()
        assertThat(mutableContact.postalAddresses).isEmpty()
        assertThat(mutableContact.webAddresses).isEmpty()
        assertThat(mutableContact.relations).isEmpty()
        assertThat(mutableContact.events).isEmpty()
    }

    @Test
    fun `should just do nothing if data to-delete is not there`() {
        val data = createContactBaseData()
        val mutableContact = someAndroidContactMutable(contactData = ContactDataContainer.createEmpty())
        val changedContact = someContactEditable(
            contactData = prepareContactDataForInternalContact(data = data, modelStatus = ModelStatus.DELETED)
        )

        underTest.updateChangedContactData(changedContact = changedContact, mutableContact = mutableContact)

        assertThat(mutableContact.phones).isEmpty()
        assertThat(mutableContact.mails).isEmpty()
        assertThat(mutableContact.postalAddresses).isEmpty()
        assertThat(mutableContact.webAddresses).isEmpty()
        assertThat(mutableContact.relations).isEmpty()
        assertThat(mutableContact.events).isEmpty()
    }

    private fun prepareContactDataForInternalContact(
        data: ContactDataContainer,
        modelStatus: ModelStatus
    ): List<ContactData> =
        listOf(
            data.phoneNumbers.mapIndexed { index, elem ->
                PhoneNumber.createEmpty(index)
                    .copy(id = someContactDataIdExternal(index), value = elem, modelStatus = modelStatus)
            },
            data.emailAddresses.mapIndexed { index, elem ->
                EmailAddress.createEmpty(index)
                    .copy(id = someContactDataIdExternal(index), value = elem, modelStatus = modelStatus)
            },
            data.physicalAddresses.mapIndexed { index, elem ->
                PhysicalAddress.createEmpty(index)
                    .copy(id = someContactDataIdExternal(index), value = elem, modelStatus = modelStatus)
            },
            data.websites.mapIndexed { index, elem ->
                Website.createEmpty(index)
                    .copy(id = someContactDataIdExternal(index), value = elem, modelStatus = modelStatus)
            },
            data.relationships.mapIndexed { index, elem ->
                Relationship.createEmpty(index)
                    .copy(id = someContactDataIdExternal(index), value = elem, modelStatus = modelStatus)
            },
            data.eventDates.mapIndexed { index, elem ->
                EventDate.createEmpty(index)
                    .copy(id = someContactDataIdExternal(index), value = elem, modelStatus = modelStatus)
            },
        ).flatten()

    /** [variant] just to add the option of having slightly different data */
    private fun createContactBaseData(variant: Boolean = false): ContactDataContainer =
        ContactDataContainer(
            phoneNumbers = listOf("123456", "456789")
                .map { if (variant) "${it}9" else it },
            emailAddresses = listOf("alpha@beta.ch", "beta@gamma.com")
                .map { if (variant) "9$it" else it },
            physicalAddresses = listOf("alpha street 53", "14 beta street, 88833 NY")
                .map { if (variant) "${it}9" else it },
            websites = listOf("www.jedi.com", "www.sith.com")
                .map { if (variant) "${it}9" else it },
            relationships = listOf("Darth Vader", "Darth Maul")
                .map { if (variant) "${it}9" else it },
            eventDates = listOf(LocalDate.now(), LocalDate.now().minusDays(1))
                .map { if (variant) it.minusYears(1) else it },
        )
}
