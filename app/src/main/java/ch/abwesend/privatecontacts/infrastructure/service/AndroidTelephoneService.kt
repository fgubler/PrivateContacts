/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.service

import android.content.Context
import android.os.Build
import android.telephony.PhoneNumberUtils
import android.telephony.TelephonyManager
import ch.abwesend.privatecontacts.domain.service.interfaces.TelephoneService
import java.util.Locale

private val INTERNATIONAL_FORMAT_START_CHARACTERS = listOf("+", "00")

class AndroidTelephoneService(applicationContext: Context) : TelephoneService {
    private val telephonyManager: TelephonyManager? by lazy {
        applicationContext.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
    }

    private val potentialCountryCodes: List<String>
        get() = listOfNotNull(telephonyManager?.networkCountryIso, Locale.getDefault().country.lowercase())

    override fun formatPhoneNumberForDisplay(number: String): String =
        potentialCountryCodes.firstNotNullOfOrNull { PhoneNumberUtils.formatNumber(number, it) } ?: number

    override fun formatPhoneNumberForMatching(number: String): String = removeSeparators(number)

    override fun removeSeparators(number: String): String =
        PhoneNumberUtils.stripSeparators(number) ?: number

    /**
     * There would also be [PhoneNumberUtils.isGlobalPhoneNumber] but that just checks whether the
     * number conforms to any phone-number format anywhere.
     */
    override fun isInInternationalFormat(number: String): Boolean = number.isInternationalFormat()

    /** It seems like the new comparison says "false" too often */
    override fun arePhoneNumbersEquivalent(
        phoneNumber1: String,
        phoneNumber2: String,
        forceLegacyComparison: Boolean,
    ): Boolean =
        if (!forceLegacyComparison && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            potentialCountryCodes.map {
                PhoneNumberUtils.areSamePhoneNumber(phoneNumber1, phoneNumber2, it)
            }.any()
        } else {
            @Suppress("DEPRECATION")
            PhoneNumberUtils.compare(phoneNumber1, phoneNumber2)
        }

    private fun String.isInternationalFormat(): Boolean {
        return INTERNATIONAL_FORMAT_START_CHARACTERS.any { startsWith(it) }
    }
}
