/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.importexport

import ch.abwesend.privatecontacts.domain.model.contact.IContactEditable

data class ContactParsedData(val successfulContacts: List<IContactEditable>, val numberOfFailedContacts: Int)
