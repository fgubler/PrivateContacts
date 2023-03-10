/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts

import ch.abwesend.privatecontacts.domain.model.ModelStatus
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactData
import ch.abwesend.privatecontacts.domain.model.contactdata.EmailAddress
import ch.abwesend.privatecontacts.domain.model.contactdata.EventDate
import ch.abwesend.privatecontacts.domain.model.contactdata.PhoneNumber
import ch.abwesend.privatecontacts.domain.model.contactdata.PhysicalAddress
import ch.abwesend.privatecontacts.domain.model.contactdata.Relationship
import ch.abwesend.privatecontacts.domain.model.contactdata.Website
import ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.service.AndroidContactChangeService
import ch.abwesend.privatecontacts.testutil.TestBase
import ch.abwesend.privatecontacts.testutil.databuilders.ContactDataContainer
import ch.abwesend.privatecontacts.testutil.databuilders.someContactDataIdExternal
import ch.abwesend.privatecontacts.testutil.databuilders.someContactEditable
import ch.abwesend.privatecontacts.testutil.databuilders.someContactImage
import ch.abwesend.privatecontacts.testutil.databuilders.someMutableAndroidContact
import ch.abwesend.privatecontacts.testutil.mockUriParse
import com.alexstyl.contactstore.ImageData
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.time.LocalDate

@ExperimentalCoroutinesApi
@ExtendWith(MockKExtension::class)
@Disabled // TODO fix problem with mocking MutableContact and re-enable
class AndroidContactChangeServiceTest : TestBase() {
    @InjectMockKs
    private lateinit var underTest: AndroidContactChangeService

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
        val mutableContact = someMutableAndroidContact()

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
        val mutableContact = someMutableAndroidContact(
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
        val mutableContact = someMutableAndroidContact(imageData = ImageData(ByteArray(size = 0)))

        underTest.updateChangedImage(changedContact = changedContact, mutableContact = mutableContact)

        assertThat(mutableContact.imageData?.raw).isNotNull.hasSize(0)
    }

    @Test
    fun `should remove deleted image`() {
        val image = someContactImage(fullImage = ByteArray(size = 42), modelStatus = ModelStatus.DELETED)
        val changedContact = someContactEditable(
            image = image
        )
        val mutableContact = someMutableAndroidContact(imageData = ImageData(ByteArray(size = 0)))

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
        val mutableContact = someMutableAndroidContact(imageData = ImageData(ByteArray(size = 0)))

        underTest.updateChangedImage(changedContact = changedContact, mutableContact = mutableContact)

        assertThat(mutableContact.imageData?.raw).isNotNull.hasSize(42)
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `should add new contact data (or update if it already exists)`(dataAlreadyExists: Boolean) {
        val oldData = if (dataAlreadyExists) createContactBaseData() else ContactDataContainer.createEmpty()
        val newData = createContactBaseData(variant = true)
        val mutableContact = someMutableAndroidContact(contactData = oldData)
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
        val mutableContact = someMutableAndroidContact(contactData = oldData)
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
    fun `should leave unchanged contact data as-is`() {
        val data = createContactBaseData()
        val mutableContact = someMutableAndroidContact(contactData = ContactDataContainer.createEmpty())
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
        val mutableContact = someMutableAndroidContact(contactData = data)
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
        val mutableContact = someMutableAndroidContact(contactData = ContactDataContainer.createEmpty())
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
