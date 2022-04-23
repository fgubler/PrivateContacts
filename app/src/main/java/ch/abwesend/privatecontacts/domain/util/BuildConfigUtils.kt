/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.util

import android.os.Build

val canReadCallingNumberFromPhoneState: Boolean
    get() = Build.VERSION.SDK_INT < Build.VERSION_CODES.P

val canUseCallScreeningService: Boolean
    get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

/**
 * Starting with version Q, the CallScreeningService is available.
 * Until version O, the incoming phone-number can be read from a broadcast without CALL_LOG permission.
 * Google does not want to give me the CALL_LOG permission...
 */
val callIdentificationPossible: Boolean
    get() = canReadCallingNumberFromPhoneState || canUseCallScreeningService
