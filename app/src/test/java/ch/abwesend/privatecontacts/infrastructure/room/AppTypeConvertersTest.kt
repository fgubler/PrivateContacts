/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.room

import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataCategory
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType
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
        val nonNullValue = ContactDataCategory.EMAIL
        val nullValue: ContactDataCategory? = null

        val serializedNonNull = underTest.serializeContactDataCategory(nonNullValue)
        val serializedNull = underTest.serializeContactDataCategory(nullValue)
        val deserializedNonNull = underTest.deserializeContactDataCategory(serializedNonNull)
        val deserializedNull = underTest.deserializeContactDataCategory(serializedNull)

        assertThat(deserializedNonNull).isEqualTo(nonNullValue)
        assertThat(deserializedNull).isEqualTo(nullValue)
    }

    @Test
    fun `should convert ContactDataType-Key`() {
        val nonNullValue = ContactDataType.Key.BUSINESS
        val nullValue: ContactDataType.Key? = null

        val serializedNonNull = underTest.serializeContactDataType(nonNullValue)
        val serializedNull = underTest.serializeContactDataType(nullValue)
        val deserializedNonNull = underTest.deserializeContactDataType(serializedNonNull)
        val deserializedNull = underTest.deserializeContactDataType(serializedNull)

        assertThat(deserializedNonNull).isEqualTo(nonNullValue)
        assertThat(deserializedNull).isEqualTo(nullValue)
    }
}
