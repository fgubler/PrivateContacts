package ch.abwesend.privatecontacts.infrastructure.room

import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataSubType
import ch.abwesend.privatecontacts.infrastructure.room.contactdata.ContactDataType
import ch.abwesend.privatecontacts.infrastructure.room.database.AppTypeConverters
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.UUID

class AppTypeConvertersTest {
    private val underTest = AppTypeConverters

    @Test
    fun `should convert UUID`() {
        val nonNullValue = UUID.randomUUID()
        val nullValue: UUID? = null

        val serializedNonNull = underTest.serializeUuid(nonNullValue)
        val serializedNull = underTest.serializeUuid(nullValue)
        val deserializedNonNull = underTest.deserializeUuid(serializedNonNull)
        val deserializedNull = underTest.deserializeUuid(serializedNull)

        assertThat(deserializedNonNull).isEqualTo(nonNullValue)
        assertThat(deserializedNull).isEqualTo(nullValue)
    }

    @Test
    fun `should convert ContactType`() {
        val nonNullValue = ContactType.PRIVATE
        val nullValue: ContactType? = null

        val serializedNonNull = underTest.serializeContactType(nonNullValue)
        val serializedNull = underTest.serializeContactType(nullValue)
        val deserializedNonNull = underTest.deserializeContactType(serializedNonNull)
        val deserializedNull = underTest.deserializeContactType(serializedNull)

        assertThat(deserializedNonNull).isEqualTo(nonNullValue)
        assertThat(deserializedNull).isEqualTo(nullValue)
    }

    @Test
    fun `should convert ContactDataType`() {
        val nonNullValue = ContactDataType.EMAIL
        val nullValue: ContactDataType? = null

        val serializedNonNull = underTest.serializeContactDataType(nonNullValue)
        val serializedNull = underTest.serializeContactDataType(nullValue)
        val deserializedNonNull = underTest.deserializeContactDataType(serializedNonNull)
        val deserializedNull = underTest.deserializeContactDataType(serializedNull)

        assertThat(deserializedNonNull).isEqualTo(nonNullValue)
        assertThat(deserializedNull).isEqualTo(nullValue)
    }

    @Test
    fun `should convert ContactDataSubType-Key`() {
        val nonNullValue = ContactDataSubType.Key.BUSINESS
        val nullValue: ContactDataSubType.Key? = null

        val serializedNonNull = underTest.serializeContactDataSubType(nonNullValue)
        val serializedNull = underTest.serializeContactDataSubType(nullValue)
        val deserializedNonNull = underTest.deserializeContactDataSubType(serializedNonNull)
        val deserializedNull = underTest.deserializeContactDataSubType(serializedNull)

        assertThat(deserializedNonNull).isEqualTo(nonNullValue)
        assertThat(deserializedNull).isEqualTo(nullValue)
    }
}
