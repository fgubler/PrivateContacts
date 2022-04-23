/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.util

import android.os.Build
import android.telephony.PhoneNumberUtils

/**
 * It seems like the new comparison says "false" too often
 */
fun arePhoneNumbersEquivalent(
    phoneNumber1: String,
    phoneNumber2: String,
    defaultCountryIsoCode: String,
    forceLegacyComparison: Boolean = false,
): Boolean =
    if (!forceLegacyComparison && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        PhoneNumberUtils.areSamePhoneNumber(
            phoneNumber1,
            phoneNumber2,
            defaultCountryIsoCode,
        )
    } else {
        @Suppress("DEPRECATION")
        PhoneNumberUtils.compare(phoneNumber1, phoneNumber2)
    }
