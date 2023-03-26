/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.service

import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType
import ch.abwesend.privatecontacts.testutil.TestBase
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExperimentalCoroutinesApi
@ExtendWith(MockKExtension::class)
class AndroidCompanyMappingServiceTest : TestBase() {
    @InjectMockKs
    private lateinit var underTest: AndroidContactCompanyMappingService

    @Test
    fun `should detect pattern for companies`() {
        val customLabels = listOf(
            "$CUSTOM_RELATIONSHIP_TYPE_ORGANISATION Blablabla",
            "${CUSTOM_RELATIONSHIP_TYPE_ORGANISATION}Blabla",
            "${CUSTOM_RELATIONSHIP_TYPE_ORGANISATION}MAIN",
            "${CUSTOM_RELATIONSHIP_TYPE_ORGANISATION}CUSTOM:Jedi Inc.",
            "The dark side",
            "Blablabla $CUSTOM_RELATIONSHIP_TYPE_ORGANISATION"
        )

        val results = customLabels.map { underTest.matchesCompanyCustomRelationshipPattern(it) }

        assertThat(results[0]).isTrue
        assertThat(results[1]).isTrue
        assertThat(results[2]).isTrue
        assertThat(results[3]).isTrue
        assertThat(results[4]).isFalse
        assertThat(results[5]).isFalse
    }

    @Test
    fun `should encode static data-type`() {
        val dataType = ContactDataType.Main

        val result = underTest.encodeToPseudoRelationshipLabel(dataType)

        assertThat(underTest.matchesCompanyCustomRelationshipPattern(result)).isTrue
        assertThat(result).contains(dataType.key.name)
    }

    @Test
    fun `should encode custom data-type`() {
        val customLabel = "Death Star"
        val dataType = ContactDataType.CustomValue(customLabel)

        val result = underTest.encodeToPseudoRelationshipLabel(dataType)

        assertThat(underTest.matchesCompanyCustomRelationshipPattern(result)).isTrue
        assertThat(result).contains(dataType.key.name)
        assertThat(result).contains(customLabel)
    }

    @Test
    fun `should decode static data-type`() {
        val dataType = ContactDataType.Main
        val encodedValue = underTest.encodeToPseudoRelationshipLabel(dataType)

        val result = underTest.decodeFromPseudoRelationshipLabel(encodedValue)

        assertThat(result).isEqualTo(dataType)
    }

    @Test
    fun `should decode custom data-type`() {
        val customLabel = "Death Star"
        val dataType = ContactDataType.CustomValue(customLabel)
        val encodedValue = underTest.encodeToPseudoRelationshipLabel(dataType)

        val result = underTest.decodeFromPseudoRelationshipLabel(encodedValue)

        assertThat(result).isEqualTo(dataType)
    }

    @Test
    fun `should decode invalid data-type to default`() {
        val invalidEncodedLabel = "This is not a valid encoding"

        val result = underTest.decodeFromPseudoRelationshipLabel(invalidEncodedLabel)

        assertThat(result).isEqualTo(ContactDataType.Business)
    }
}
