package ch.abwesend.privatecontacts.infrastructure.room.contact

import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataCategory
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType
import ch.abwesend.privatecontacts.infrastructure.room.contactdata.toEntity
import ch.abwesend.privatecontacts.testutil.TestBase
import ch.abwesend.privatecontacts.testutil.someContactId
import ch.abwesend.privatecontacts.testutil.someEmailAddress
import ch.abwesend.privatecontacts.testutil.somePhoneNumber
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

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
}
