/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.importexport

import ch.abwesend.privatecontacts.domain.model.contact.IContact

data class ContactParsedData(val successfulContacts: List<IContact>, val numberOfFailedContacts: Int)
