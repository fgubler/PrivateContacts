/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.util

import android.os.Build
import android.telephony.PhoneNumberUtils
import java.util.Locale

fun arePhoneNumbersEquivalent(
    phoneNumber1: String,
    phoneNumber2: String,
    defaultCountryIsoCode: String?,
): Boolean =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val defaultCountryIso = defaultCountryIsoCode ?: Locale.getDefault().country.lowercase()
        PhoneNumberUtils.areSamePhoneNumber(
            phoneNumber1,
            phoneNumber2,
            defaultCountryIso,
        )
    } else {
        PhoneNumberUtils.compare(phoneNumber1, phoneNumber2)
    }
