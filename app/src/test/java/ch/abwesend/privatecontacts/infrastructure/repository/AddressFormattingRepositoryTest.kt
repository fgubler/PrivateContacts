/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository

import ch.abwesend.privatecontacts.domain.util.Constants
import ch.abwesend.privatecontacts.infrastructure.repository.addressformatting.AddressFormattingRepository
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
class AddressFormattingRepositoryTest : TestBase() {
    private lateinit var underTest: AddressFormattingRepository
    private val linebreak = ",${Constants.linebreak}"

    private lateinit var previousLocale: Locale

    override fun setup() {
        super.setup()
        underTest = AddressFormattingRepository()
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

    private fun throwInvalidCountry(country: String): Nothing =
        throw IllegalArgumentException("Invalid countryCode '$country'")
}
