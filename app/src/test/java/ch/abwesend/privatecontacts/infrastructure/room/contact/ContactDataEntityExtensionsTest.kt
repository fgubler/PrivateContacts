/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.room.contact

import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataCategory
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType
import ch.abwesend.privatecontacts.infrastructure.room.contactdata.toEntity
import ch.abwesend.privatecontacts.testutil.TestBase
import ch.abwesend.privatecontacts.testutil.databuilders.someContactId
import ch.abwesend.privatecontacts.testutil.databuilders.someEmailAddress
import ch.abwesend.privatecontacts.testutil.databuilders.someEventDate
import ch.abwesend.privatecontacts.testutil.databuilders.somePhoneNumber
import ch.abwesend.privatecontacts.testutil.uuid
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.LocalDate

@ExperimentalCoroutinesApi
@ExtendWith(MockKExtension::class)
class ContactDataEntityExtensionsTest : TestBase() {
    @Test
    fun `phone number should be copied correctly`() {
        val contactData = somePhoneNumber()
        val contactId = someContactId()

        val entity = contactData.toEntity(contactId)

        assertThat(entity.id).isEqualTo(contactData.id.uuid)
        assertThat(entity.category).isEqualTo(ContactDataCategory.PHONE_NUMBER)
        assertThat(entity.sortOrder).isEqualTo(contactData.sortOrder)
        assertThat(entity.type.key).isEqualTo(contactData.type.key)
        assertThat(entity.type.customValue).isEqualTo((contactData.type as? ContactDataType.CustomValue)?.customValue)
        assertThat(entity.valueRaw).isEqualTo(contactData.value)
        assertThat(entity.valueFormatted).isEqualTo(contactData.formattedValue)
        assertThat(entity.valueForMatching).isEqualTo(contactData.valueForMatching)
        assertThat(entity.isMain).isEqualTo(contactData.isMain)
    }

    @Test
    fun `email should be copied correctly`() {
        val contactData = someEmailAddress()
        val contactId = someContactId()

        val entity = contactData.toEntity(contactId)

        assertThat(entity.id).isEqualTo(contactData.id.uuid)
        assertThat(entity.category).isEqualTo(ContactDataCategory.EMAIL)
        assertThat(entity.sortOrder).isEqualTo(contactData.sortOrder)
        assertThat(entity.type.key).isEqualTo(contactData.type.key)
        assertThat(entity.type.customValue).isEqualTo((contactData.type as? ContactDataType.CustomValue)?.customValue)
        assertThat(entity.valueRaw).isEqualTo(contactData.value)
        assertThat(entity.valueFormatted).isEqualTo(contactData.formattedValue)
        assertThat(entity.isMain).isEqualTo(contactData.isMain)
    }

    @Test
    fun `event-date should be copied correctly`() {
        val contactData = someEventDate(value = LocalDate.of(2022, 5, 1))
        val contactId = someContactId()

        val entity = contactData.toEntity(contactId)

        assertThat(entity.id).isEqualTo(contactData.id.uuid)
        assertThat(entity.category).isEqualTo(ContactDataCategory.EVENT_DATE)
        assertThat(entity.sortOrder).isEqualTo(contactData.sortOrder)
        assertThat(entity.type.key).isEqualTo(contactData.type.key)
        assertThat(entity.type.customValue).isEqualTo((contactData.type as? ContactDataType.CustomValue)?.customValue)
        assertThat(entity.isMain).isEqualTo(contactData.isMain)
        assertThat(entity.valueRaw).isEqualTo("2022-05-01")
    }
}
