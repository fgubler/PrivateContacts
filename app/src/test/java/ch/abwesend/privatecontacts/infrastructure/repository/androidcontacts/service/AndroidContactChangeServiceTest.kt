/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.service

import ch.abwesend.privatecontacts.domain.model.ModelStatus
import ch.abwesend.privatecontacts.domain.model.ModelStatus.CHANGED
import ch.abwesend.privatecontacts.domain.model.ModelStatus.NEW
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactData
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.Business
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.CustomValue
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.Main
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.Other
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
import com.alexstyl.contactstore.Label
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
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

    @MockK
    private lateinit var companyMappingService: AndroidContactCompanyMappingService

    @InjectMockKs
    private lateinit var underTest: AndroidContactChangeService

    override fun setupKoinModule(module: Module) {
        super.setupKoinModule(module)
        module.single { mutableContactFactory }
        module.single { companyMappingService }
    }

    override fun setup() {
        super.setup()
        every { companyMappingService.mapToPseudoRelationshipLabel(any()) } answers {
            val type: ContactDataType = firstArg()
            val key = type.key
            if (type is CustomValue) "${key.name}-${type.customValue}"
            else key.name
        }
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
        val modelStatus = if (statusNew) NEW else CHANGED
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
            contactData = prepareContactDataForInternalContact(data = newData, modelStatus = NEW)
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
            contactData = prepareContactDataForInternalContact(data = newData, modelStatus = CHANGED)
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
    fun `should add new companies as relations`() {
        val customType = "Dark Side"
        val companies = listOf(
            someCompany(value = "Company A", type = Main, modelStatus = NEW, sortOrder = 0),
            someCompany(value = "Company B", type = Other, modelStatus = NEW, sortOrder = 1),
            someCompany(value = "Company C", type = Business, modelStatus = NEW, sortOrder = 2),
            someCompany(value = "Company D", type = CustomValue(customType), modelStatus = NEW, sortOrder = 3),
        )
        val mutableContact = someAndroidContactMutable()
        val changedContact = someContactEditable(contactData = companies)
        mockUriParse()

        underTest.updateChangedContactData(changedContact = changedContact, mutableContact = mutableContact)

        assertThat(mutableContact.relations).hasSameSizeAs(companies)
        mutableContact.relations.indices.forEach { index ->
            val relation = mutableContact.relations[index]
            val company = companies[index]
            assertThat(relation.value.name)
                .describedAs("Company '${company.value}'")
                .isEqualTo(company.value)
            assertThat(relation.label).isInstanceOf(Label.Custom::class.java)
            assertThat((relation.label as Label.Custom).label)
                .describedAs("Company '${company.value}'")
                .contains(company.type.key.name)
        }
        assertThat((mutableContact.relations.last().label as Label.Custom).label)
            .contains((companies.last().type as CustomValue).customValue)
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
