package ch.abwesend.privatecontacts.testutil

import ch.abwesend.privatecontacts.domain.service.interfaces.TelephoneService
import java.util.Locale

class TestTelephoneService : TelephoneService {
    override val telephoneDefaultCountryIso: String
        get() = Locale.getDefault().country.lowercase()
    override fun formatPhoneNumberForDisplay(number: String): String = number
    override fun formatPhoneNumberForMatching(number: String): String = number
}
