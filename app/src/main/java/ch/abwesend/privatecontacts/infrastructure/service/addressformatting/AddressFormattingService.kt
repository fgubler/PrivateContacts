/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.service.addressformatting

import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.service.interfaces.IAddressFormattingService
import ch.abwesend.privatecontacts.domain.util.Constants
import com.google.i18n.addressinput.common.AddressData
import com.google.i18n.addressinput.common.FormOptions
import com.google.i18n.addressinput.common.FormatInterpreter
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
        useFallbackForEmptyAddress: Boolean,
    ): String {
        val currentLocale = Locale.getDefault()
        val validCountry = isValidCountryCode(country)
        val countryCode = if (validCountry) country else currentLocale.country
        val languageCode = if (validCountry) null else currentLocale.language

        val addressFragments = getFormattedAddressFragments(
            street = street,
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
            .joinToString(separator = ",${Constants.linebreak}")
            .trim()
    }

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
