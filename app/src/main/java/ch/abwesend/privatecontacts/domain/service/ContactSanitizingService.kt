/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.service

import ch.abwesend.privatecontacts.domain.model.contact.IContactEditable
import ch.abwesend.privatecontacts.domain.model.contactdata.PhoneNumber
import ch.abwesend.privatecontacts.domain.service.interfaces.ITelephoneService
import ch.abwesend.privatecontacts.domain.util.injectAnywhere

class ContactSanitizingService {
    private val telephoneService: ITelephoneService by injectAnywhere()

    fun sanitizeContact(contact: IContactEditable) {
        contact.contactDataSet.indices.forEach { i ->
            val data = contact.contactDataSet[i]
            if (data is PhoneNumber) {
                val formattedValue = telephoneService.formatPhoneNumberForDisplay(data.value)
                val valueForMatching = telephoneService.formatPhoneNumberForMatching(data.value)
                contact.contactDataSet[i] = data.copy(
                    formattedValue = formattedValue,
                    valueForMatching = valueForMatching
                )
            }
        }
    }
}
