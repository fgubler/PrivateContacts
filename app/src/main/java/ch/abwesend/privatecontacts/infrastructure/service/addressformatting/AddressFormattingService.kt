/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.service.addressformatting

import ch.abwesend.privatecontacts.domain.service.interfaces.IAddressFormattingService
import ch.abwesend.privatecontacts.domain.util.Constants.linebreak
import java.util.Locale

/**
 * See https://stackoverflow.com/questions/11269172/address-formatting-based-on-locale-in-android
 */
class AddressFormattingService : IAddressFormattingService {
    private val countryCodes: Set<String> by lazy {
        Locale.getISOCountries().toSet()
    }

    /**
     * [neighborhood] will be ignored by the library
     * [country] important for the locale but will not be part of the returned address
     */
    override fun formatAddress(
        street: String,
        neighborhood: String,
        postalCode: String,
        city: String,
        region: String,
        country: String,
    ): String {
        if ((street + neighborhood + postalCode + city + region + country).isEmpty()) {
            return ""
        }

        val currentLocale = Locale.getDefault()
        val validCountry = isValidCountryCode(country)
        val countryCode = if (validCountry) country else currentLocale.country

        val addressFragments = getAddressFragments(
            street = street,
            neighborhood = neighborhood,
            postalCode = postalCode,
            city = city,
            region = region,
            countryCode = countryCode,
        )

        return addressFragments
            .filterNot { it.isEmpty() }
            .map { it.trimEnd(',') }
            .joinToString(separator = ",$linebreak")
            .trim()
    }

    private fun getAddressFragments(
        street: String,
        neighborhood: String,
        postalCode: String,
        city: String,
        region: String,
        countryCode: String,
    ): List<String> {
        val cityWithPostalCode = listOf(postalCode, city)
            .filterNot { it.isEmpty() }
            .joinToString(separator = " ")

        val addressFragments = listOfNotNull(
            street,
            neighborhood,
            cityWithPostalCode,
            region,
            countryCode
        )

        return addressFragments
            .map { it.trim() }
            .filterNot { it.isEmpty() }
    }

    private fun isValidCountryCode(country: String): Boolean = countryCodes.contains(country)
}
