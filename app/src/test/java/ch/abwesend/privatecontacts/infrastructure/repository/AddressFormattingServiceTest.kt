/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository

import ch.abwesend.privatecontacts.domain.util.Constants
import ch.abwesend.privatecontacts.infrastructure.service.addressformatting.AddressFormattingService
import ch.abwesend.privatecontacts.testutil.TestBase
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.util.Locale

private const val DE = "DE"
private const val US = "US"

@ExperimentalCoroutinesApi
@ExtendWith(MockKExtension::class)
class AddressFormattingServiceTest : TestBase() {
    private lateinit var underTest: AddressFormattingService
    private val linebreak = ",${Constants.linebreak}"

    private lateinit var previousLocale: Locale

    override fun setup() {
        super.setup()
        underTest = AddressFormattingService()
        previousLocale = Locale.getDefault()
        Locale.setDefault(Locale.GERMANY)
    }

    override fun tearDown() {
        super.tearDown()
        Locale.setDefault(previousLocale)
    }

    @ParameterizedTest
    @ValueSource(strings = [DE, US])
    fun `will ignore the neighborhood completely for DE and US`(countryCode: String) {
        val street = ""
        val neighborhood = "This is a great neighborhood"
        val postalCode = ""
        val city = ""
        val region = ""
        val expectedAddress = ""

        val result = underTest.formatAddress(
            street = street,
            neighborhood = neighborhood,
            postalCode = postalCode,
            city = city,
            region = region,
            country = countryCode,
            useFallbackForEmptyAddress = false,
        )

        assertThat(result).isEqualTo(expectedAddress)
    }

    @ParameterizedTest
    @ValueSource(strings = [DE, US])
    fun `will not print the country into the address for DE and US`(countryCode: String) {
        val street = ""
        val neighborhood = ""
        val postalCode = ""
        val city = ""
        val region = ""
        val expectedAddress = ""

        val result = underTest.formatAddress(
            street = street,
            neighborhood = neighborhood,
            postalCode = postalCode,
            city = city,
            region = region,
            country = countryCode,
            useFallbackForEmptyAddress = false,
        )

        assertThat(result).isEqualTo(expectedAddress)
    }

    @ParameterizedTest
    @ValueSource(strings = [DE, US])
    fun `will ignore the region for DE but not US`(countryCode: String) {
        val street = ""
        val neighborhood = ""
        val postalCode = ""
        val city = ""
        val region = "Superb region"
        val expectedAddress = when (countryCode) {
            DE -> ""
            US -> region
            else -> throwInvalidCountry(countryCode)
        }

        val result = underTest.formatAddress(
            street = street,
            neighborhood = neighborhood,
            postalCode = postalCode,
            city = city,
            region = region,
            country = countryCode,
            useFallbackForEmptyAddress = false,
        )

        assertThat(result).isEqualTo(expectedAddress)
    }

    @ParameterizedTest
    @ValueSource(strings = [DE, US])
    fun `should format street`(countryCode: String) {
        val street = "Mainstreet 1"
        val neighborhood = ""
        val postalCode = ""
        val city = ""
        val region = ""
        val expectedAddress = street

        val result = underTest.formatAddress(
            street = street,
            neighborhood = neighborhood,
            postalCode = postalCode,
            city = city,
            region = region,
            country = countryCode,
            useFallbackForEmptyAddress = false,
        )

        assertThat(result).isEqualTo(expectedAddress)
    }

    @ParameterizedTest
    @ValueSource(strings = [DE, US])
    fun `should format street and city`(countryCode: String) {
        val street = "Mainstreet 1"
        val neighborhood = ""
        val postalCode = ""
        val city = "London"
        val region = ""
        val expectedAddress = "$street$linebreak$city"

        val result = underTest.formatAddress(
            street = street,
            neighborhood = neighborhood,
            postalCode = postalCode,
            city = city,
            region = region,
            country = countryCode,
            useFallbackForEmptyAddress = false,
        )

        assertThat(result).isEqualTo(expectedAddress)
    }

    @ParameterizedTest
    @ValueSource(strings = [DE, US])
    fun `should format street and postal code and city`(countryCode: String) {
        val street = "Mainstreet 1"
        val neighborhood = ""
        val postalCode = "8000"
        val city = "London"
        val region = ""
        val expectedAddress = when (countryCode) {
            DE -> "$street$linebreak$postalCode $city"
            US -> "$street$linebreak$city $postalCode"
            else -> throwInvalidCountry(countryCode)
        }

        val result = underTest.formatAddress(
            street = street,
            neighborhood = neighborhood,
            postalCode = postalCode,
            city = city,
            region = region,
            country = countryCode,
            useFallbackForEmptyAddress = false,
        )

        assertThat(result).isEqualTo(expectedAddress)
    }

    @ParameterizedTest
    @ValueSource(strings = [DE, US])
    fun `should format street and postal code and city and region`(countryCode: String) {
        val street = "Mainstreet 1"
        val neighborhood = ""
        val postalCode = "8000"
        val city = "London"
        val region = "Superbia"
        val expectedAddress = when (countryCode) {
            DE -> "$street$linebreak$postalCode $city"
            US -> "$street$linebreak$city, $region $postalCode"
            else -> throwInvalidCountry(countryCode)
        }

        val result = underTest.formatAddress(
            street = street,
            neighborhood = neighborhood,
            postalCode = postalCode,
            city = city,
            region = region,
            country = countryCode,
            useFallbackForEmptyAddress = false,
        )

        assertThat(result).isEqualTo(expectedAddress)
    }

    @Test
    fun `should replace invalid country-codes with the current one`() {
        val street = "Mainstreet 1"
        val neighborhood = ""
        val postalCode = "8000"
        val city = "London"
        val region = "Superbia"
        val expectedAddress = "$street$linebreak$postalCode $city"

        val result = underTest.formatAddress(
            street = street,
            neighborhood = neighborhood,
            postalCode = postalCode,
            city = city,
            region = region,
            country = "something really invalid",
            useFallbackForEmptyAddress = false,
        )

        assertThat(result).isEqualTo(expectedAddress)
    }

    @ParameterizedTest
    @ValueSource(strings = [DE, US])
    fun `should use the fallback if the lib-logic returns an empty string`(countryCode: String) {
        val street = ""
        val neighborhood = "This is a great neighborhood"
        val postalCode = ""
        val city = ""
        val region = ""
        val expectedAddress = "$neighborhood$linebreak$countryCode"

        val result = underTest.formatAddress(
            street = street,
            neighborhood = neighborhood,
            postalCode = postalCode,
            city = city,
            region = region,
            country = countryCode,
            useFallbackForEmptyAddress = true,
        )

        assertThat(result).isEqualTo(expectedAddress)
    }

    @ParameterizedTest
    @ValueSource(strings = [DE, US])
    fun `should be able to handle the entire address being in the street field`(countryCode: String) {
        val street = """
            Bahnhofstrasse 15
            8000 Zuerich
            Zuerich
            Schweiz
        """.trimIndent()
        val neighborhood = ""
        val postalCode = ""
        val city = ""
        val region = ""
        val expectedAddress = """
            Bahnhofstrasse 15,
            8000 Zuerich,
            Zuerich,
            Schweiz
        """.trimIndent()

        val result = underTest.formatAddress(
            street = street,
            neighborhood = neighborhood,
            postalCode = postalCode,
            city = city,
            region = region,
            country = countryCode,
            useFallbackForEmptyAddress = false,
        )

        val cleanedResult = result.replace("\r", "") // linebreaks may be \r\n...
        assertThat(cleanedResult).isEqualTo(expectedAddress)
    }

    @ParameterizedTest
    @ValueSource(strings = [DE, US])
    fun `should be able to handle address with neighborhood being in the street field`(countryCode: String) {
        val street = """
            Bahnhofstrasse 15
            Wiedikon
            8000 Zuerich
            Zuerich
            Schweiz
        """.trimIndent()
        val neighborhood = ""
        val postalCode = ""
        val city = ""
        val region = ""
        val expectedAddress = """
            Bahnhofstrasse 15,
            Wiedikon,
            8000 Zuerich,
            Zuerich,
            Schweiz
        """.trimIndent()

        val result = underTest.formatAddress(
            street = street,
            neighborhood = neighborhood,
            postalCode = postalCode,
            city = city,
            region = region,
            country = countryCode,
            useFallbackForEmptyAddress = false,
        )

        val cleanedResult = result.replace("\r", "") // linebreaks may be \r\n...
        assertThat(cleanedResult).isEqualTo(expectedAddress)
    }

    @ParameterizedTest
    @ValueSource(strings = [DE, US])
    fun `should be able to handle a partial address being in the street field`(countryCode: String) {
        val street = """
            Bahnhofstrasse 15
            Zuerich
        """.trimIndent()
        val neighborhood = ""
        val postalCode = ""
        val city = ""
        val region = ""
        val expectedAddress = """
            Bahnhofstrasse 15,
            Zuerich
        """.trimIndent()

        val result = underTest.formatAddress(
            street = street,
            neighborhood = neighborhood,
            postalCode = postalCode,
            city = city,
            region = region,
            country = countryCode,
            useFallbackForEmptyAddress = false,
        )

        val cleanedResult = result.replace("\r", "") // linebreaks may be \r\n...
        assertThat(cleanedResult).isEqualTo(expectedAddress)
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `should fix the formatting of the street for German locale`(isGermanLocale: Boolean) {
        if (isGermanLocale) Locale.setDefault(Locale.GERMANY)
        else Locale.setDefault(Locale.US)

        val streets = listOf(
            "123 Bahnhofstrasse",
            "123 Bahnhof-strasse",
            "123 Bahnhofstraße",
            "123 Bahnhofstr",
            "123 Bahnhofstr.",
            "Bahnhofstr. 123",
        )
        val expectedGermanAddresses = listOf(
            "Bahnhofstrasse 123",
            "Bahnhof-strasse 123",
            "Bahnhofstraße 123",
            "Bahnhofstr 123",
            "Bahnhofstr. 123",
            "Bahnhofstr. 123",
        )
        val expectedAmericanAddresses = streets

        val results = streets.map { street ->
            underTest.formatAddress(
                street = street,
                neighborhood = "",
                postalCode = "",
                city = "",
                region = "",
                country = "",
                useFallbackForEmptyAddress = false,
            )
        }

        assertThat(results).hasSameSizeAs(streets)
        results.indices.forEach { index ->
            if (isGermanLocale) {
                assertThat(results[index]).isEqualTo(expectedGermanAddresses[index])
            } else {
                assertThat(results[index]).isEqualTo(expectedAmericanAddresses[index])
            }
        }
    }

    @Test
    fun `should leave the formatting of the street unchanged if it does not have exactly the expected format`() {
        val streets = listOf(
            "123 Bahnhofstrasse${Constants.linebreak}Zürich",
            "123 Bahnhofstrasse ${Constants.linebreak} Zürich",
            "123 Bahnhofstrasse,Zürich",
            "123 Bahnhofstrasse, Zürich",
            "123 Bahnhof",
            "123 Bahnhofstr. in Zürich",
            "Bahnhofstrasse 123",
        )
        val expectedStreets = listOf(
            "123 Bahnhofstrasse,${Constants.linebreak}Zürich",
            "123 Bahnhofstrasse, ${Constants.linebreak} Zürich",
            "123 Bahnhofstrasse,Zürich",
            "123 Bahnhofstrasse, Zürich",
            "123 Bahnhof",
            "123 Bahnhofstr. in Zürich",
            "Bahnhofstrasse 123",
        )

        val results = streets.map { street ->
            underTest.formatAddress(
                street = street,
                neighborhood = "",
                postalCode = "",
                city = "",
                region = "",
                country = "",
                useFallbackForEmptyAddress = false,
            )
        }

        assertThat(results).hasSameSizeAs(streets)
        results.indices.forEach { index ->
            val result = results[index].replace(" ", "")
            val expected = expectedStreets[index].replace(" ", "")
            assertThat(result).isEqualTo(expected)
        }
    }

    private fun throwInvalidCountry(country: String): Nothing =
        throw IllegalArgumentException("Invalid countryCode '$country'")
}
