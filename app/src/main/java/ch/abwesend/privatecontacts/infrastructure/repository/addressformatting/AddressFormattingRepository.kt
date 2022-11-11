package ch.abwesend.privatecontacts.infrastructure.repository.addressformatting

import ch.abwesend.privatecontacts.domain.model.contactdata.PhysicalAddressDto
import ch.abwesend.privatecontacts.domain.repository.IAddressFormattingRepository
import ch.abwesend.privatecontacts.domain.util.Constants
import com.google.i18n.addressinput.common.AddressData
import com.google.i18n.addressinput.common.FormOptions
import com.google.i18n.addressinput.common.FormatInterpreter
import java.util.Locale

/**
 * See https://stackoverflow.com/questions/11269172/address-formatting-based-on-locale-in-android
 */
class AddressFormattingRepository : IAddressFormattingRepository {
    private val countryCodes: Set<String> by lazy {
        Locale.getISOCountries().toSet()
    }

    override fun formatAddress(address: PhysicalAddressDto): String {
        val currentLocale = Locale.getDefault()
        val validCountry = isValidCountryCode(address.country)
        val countryCode = if (validCountry) address.country else currentLocale.country
        val languageCode = if (validCountry) null else currentLocale.language

        val formatInterpreter = FormatInterpreter(FormOptions().createSnapshot())
        val builder = AddressData.Builder()
            .setAddress(address.street)
            .setDependentLocality(address.neighborhood)
            .setLocality(address.city)
            .setPostalCode(address.postalCode)
            .setAdminArea(address.region)
            .setCountry(countryCode)

        languageCode?.let { builder.setLanguageCode(it) }
        val addressData = builder.build()

        val addressFragments: List<String?> = formatInterpreter.getEnvelopeAddress(addressData)
        return addressFragments.joinToString(separator = Constants.linebreak)
    }

    private fun isValidCountryCode(country: String): Boolean = countryCodes.contains(country)
}
