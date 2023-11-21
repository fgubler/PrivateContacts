/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.service.addressformatting

import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.service.interfaces.IAddressFormattingService
import ch.abwesend.privatecontacts.domain.util.Constants.linebreak
import com.google.i18n.addressinput.common.AddressData
import com.google.i18n.addressinput.common.FormOptions
import com.google.i18n.addressinput.common.FormatInterpreter
import java.util.Locale

private val streetSuffixes = listOf("strasse", "straße", "str", "str.")

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
        useFallbackForEmptyAddress: Boolean,
    ): String {
        if ((street + neighborhood + postalCode + city + region + country).isEmpty()) {
            return ""
        }

        val currentLocale = Locale.getDefault()
        val validCountry = isValidCountryCode(country)
        val countryCode = if (validCountry) country else currentLocale.country
        val languageCode = if (validCountry) null else currentLocale.language
        val reformattedStreet = reformatStreetForGerman(street = street, locale = currentLocale)

        val addressFragments = getFormattedAddressFragments(
            street = reformattedStreet,
            neighborhood = neighborhood,
            postalCode = postalCode,
            city = city,
            region = region,
            countryCode = countryCode,
            languageCode = languageCode,
        ).ifEmpty {
            if (useFallbackForEmptyAddress) {
                getFallbackAddressFragments(
                    street = street,
                    neighborhood = neighborhood,
                    postalCode = postalCode,
                    city = city,
                    region = region,
                    countryCode = countryCode,
                )
            } else emptyList()
        }

        return addressFragments
            .filterNot { it.isEmpty() }
            .map { it.trimEnd(',') }
            .joinToString(separator = ",$linebreak")
            .trim()
    }

    /**
     * In English we write "123 someroad" whereas in German we write "someroad 123".
     * The contact-library always opts for the English formatting.
     * For the case that the street really does only contain the street, change the ordering for German.
     */
    private fun reformatStreetForGerman(street: String, locale: Locale): String =
        if (locale.language == Locale.GERMAN.language) {
            logger.debug("checking for reformatting for street in German")
            val words = street.split(" ")

            if (!street.contains(linebreak) && !street.contains(",") && words.size == 2) { // street-name & number
                val first = words[0]
                val second = words[1]
                val isNumberFirst = first.toIntOrNull() != null
                val isStreetSecond = streetSuffixes.any { second.endsWith(it, ignoreCase = true) }

                if (isNumberFirst && isStreetSecond) {
                    logger.debug("changing ordering of street-name and street-number")
                    "$second $first"
                } else street
            } else street
        } else street

    private fun getFormattedAddressFragments(
        street: String,
        neighborhood: String,
        postalCode: String,
        city: String,
        region: String,
        countryCode: String,
        languageCode: String?,
    ): List<String> =
        try {
            val formatInterpreter = FormatInterpreter(FormOptions().createSnapshot())
            val builder = AddressData.Builder()
                .setAddress(street)
                .setDependentLocality(neighborhood)
                .setLocality(city)
                .setPostalCode(postalCode)
                .setAdminArea(region)
                .setCountry(countryCode)

            languageCode?.let { builder.setLanguageCode(it) }
            val addressData = builder.build()

            val addressFragmentsRaw: List<String?> = formatInterpreter.getEnvelopeAddress(addressData).orEmpty()
            addressFragmentsRaw
                .mapNotNull { it?.trim() }
                .filterNot { it.isEmpty() }
        } catch (e: Exception) {
            logger.warning("Failed to format address: caught exception from the Java-Code", e)
            emptyList()
        }

    private fun getFallbackAddressFragments(
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
