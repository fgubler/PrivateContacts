/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.service

import android.content.Context
import android.telephony.PhoneNumberUtils
import android.telephony.TelephonyManager
import ch.abwesend.privatecontacts.domain.service.interfaces.TelephoneService
import java.util.Locale

class AndroidTelephoneService(applicationContext: Context) : TelephoneService {
    private val telephonyManager: TelephonyManager? by lazy {
        applicationContext.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
    }

    override val telephoneDefaultCountryIso: String
        get() = telephonyManager?.networkCountryIso ?: Locale.getDefault().country.lowercase()

    override fun formatPhoneNumberForDisplay(number: String): String =
        PhoneNumberUtils.formatNumber(number, telephoneDefaultCountryIso) ?: number

    override fun formatPhoneNumberForMatching(number: String): String =
        PhoneNumberUtils.stripSeparators(number) ?: number
}
