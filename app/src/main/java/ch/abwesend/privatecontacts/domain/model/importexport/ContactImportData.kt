/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.importexport

import ch.abwesend.privatecontacts.domain.model.contact.IContact
import ch.abwesend.privatecontacts.domain.model.contact.IContactEditable
import ch.abwesend.privatecontacts.domain.model.result.ContactChangeError
import ch.abwesend.privatecontacts.domain.model.result.ContactValidationError

data class ContactImportData(
    val newImportedContacts: List<IContact>,
    val replacedExistingContacts: List<IContact>,

    val importValidationFailures: Map<IContact, List<ContactValidationError>>,
    val importFailures: Map<IContact, List<ContactChangeError>>,

    val numberOfParsingFailures: Int,
)

sealed interface ContactImportPartialData {
    data class ParsedData(
        val successfulContacts: List<IContactEditable>,
        val numberOfFailedContacts: Int
    ) : ContactImportPartialData

    data class SavedData(
        val newImportedContacts: List<IContact>,
        val importValidationFailures: Map<IContact, List<ContactValidationError>>,
        val importFailures: Map<IContact, List<ContactChangeError>>,

        val replacedExistingContacts: List<IContact>,
    )
}
