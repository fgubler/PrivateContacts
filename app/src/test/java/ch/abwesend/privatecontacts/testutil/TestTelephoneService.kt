package ch.abwesend.privatecontacts.testutil

import ch.abwesend.privatecontacts.domain.service.interfaces.TelephoneService

class TestTelephoneService : TelephoneService {
    override fun formatPhoneNumberForDisplay(number: String): String = number
    override fun formatPhoneNumberForMatching(number: String): String = number
    override fun arePhoneNumbersEquivalent(
        phoneNumber1: String,
        phoneNumber2: String,
        forceLegacyComparison: Boolean,
    ): Boolean = phoneNumber1.filter { it.isDigit() } == phoneNumber2.filter { it.isDigit() }
}
