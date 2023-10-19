/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.service

import ch.abwesend.privatecontacts.domain.model.contact.IContactEditable
import ch.abwesend.privatecontacts.domain.model.contactdata.PhoneNumber
import ch.abwesend.privatecontacts.domain.service.interfaces.TelephoneService
import ch.abwesend.privatecontacts.domain.util.injectAnywhere

class ContactSanitizingService {
    private val telephoneService: TelephoneService by injectAnywhere()

    fun sanitizeContact(contact: IContactEditable) {
        contact.contactDataSet.indices.forEach { i ->
            val data = contact.contactDataSet[i]
            if (data is PhoneNumber) {
                contact.contactDataSet[i] = sanitizePhoneNumber(data)
            }
        }
    }

    fun sanitizePhoneNumber(phoneNumber: PhoneNumber): PhoneNumber {
        val formattedValue = telephoneService.formatPhoneNumberForDisplay(phoneNumber.value)
        val valueForMatching = telephoneService.formatPhoneNumberForMatching(phoneNumber.value)
        return phoneNumber.copy(
            formattedValue = formattedValue,
            valueForMatching = valueForMatching
        )
    }
}
