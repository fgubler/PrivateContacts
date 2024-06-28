/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.service.interfaces

interface TelephoneService {
    fun formatPhoneNumberForDisplay(number: String): String
    fun formatPhoneNumberForMatching(number: String): String
    fun removeSeparators(number: String): String
    fun isInInternationalFormat(number: String): Boolean
    fun arePhoneNumbersEquivalent(
        phoneNumber1: String,
        phoneNumber2: String,
        forceLegacyComparison: Boolean = false,
    ): Boolean
}
