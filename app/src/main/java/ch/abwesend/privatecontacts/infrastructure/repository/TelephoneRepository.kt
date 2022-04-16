/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository

import android.content.Context
import android.telephony.TelephonyManager
import ch.abwesend.privatecontacts.domain.repository.ITelephoneRepository
import java.util.Locale

class TelephoneRepository(applicationContext: Context) : ITelephoneRepository {
    private val telephonyManager: TelephonyManager? by lazy {
        applicationContext.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
    }

    override val telephoneDefaultCountryIso: String
        get() = telephonyManager?.networkCountryIso ?: Locale.getDefault().country.lowercase()
}
