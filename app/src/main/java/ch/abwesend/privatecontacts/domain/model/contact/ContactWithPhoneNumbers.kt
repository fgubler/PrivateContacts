/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.contact

import ch.abwesend.privatecontacts.domain.model.contactdata.PhoneNumberValue

/**
 * Specific data-container for use-cases where only the base-data and the phone-numbers are relevant
 */
data class ContactWithPhoneNumbers(
    private val contactBase: IContactBase,
    val phoneNumbers: List<PhoneNumberValue>,
) : IContactBase by contactBase
