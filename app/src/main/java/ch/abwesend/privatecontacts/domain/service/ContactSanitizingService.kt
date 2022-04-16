/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.service

import android.telephony.PhoneNumberUtils
import ch.abwesend.privatecontacts.domain.model.contact.IContactEditable
import ch.abwesend.privatecontacts.domain.model.contactdata.PhoneNumber
import ch.abwesend.privatecontacts.domain.repository.ITelephoneRepository
import ch.abwesend.privatecontacts.domain.util.injectAnywhere

class ContactSanitizingService {
    private val telephoneRepository: ITelephoneRepository by injectAnywhere()

    fun sanitizeContact(contact: IContactEditable) {
        contact.contactDataSet.indices.forEach { i ->
            val data = contact.contactDataSet[i]
            if (data is PhoneNumber) {
                val isoCode = telephoneRepository.telephoneDefaultCountryIso
                val formattedValue = PhoneNumberUtils.formatNumber(data.value, isoCode)
                contact.contactDataSet[i] = data.copy(formattedValue = formattedValue)
            }
        }
    }
}
