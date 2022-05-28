/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.service.interfaces

interface TelephoneService {
    val telephoneDefaultCountryIso: String

    fun formatPhoneNumberForDisplay(number: String): String
    fun formatPhoneNumberForMatching(number: String): String
}
