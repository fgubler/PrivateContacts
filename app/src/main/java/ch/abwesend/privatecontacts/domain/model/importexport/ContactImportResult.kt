/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.importexport

import ch.abwesend.privatecontacts.domain.model.contact.IContact
import ch.abwesend.privatecontacts.domain.model.contact.IContactEditable
import ch.abwesend.privatecontacts.domain.model.result.ContactChangeError

data class ContactImportResult(
    val newImportedContacts: List<IContact>,
    val existingIgnoredContacts: List<IContactEditable>,
    val existingReplacedContacts: List<IContact>,

    val numberOfParsingFailures: Int,
    val importFailures: Map<IContact, ContactChangeError>,
)
