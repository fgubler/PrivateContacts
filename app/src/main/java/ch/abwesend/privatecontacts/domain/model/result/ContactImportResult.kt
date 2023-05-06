/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.result

import ch.abwesend.privatecontacts.domain.model.contact.IContact

sealed interface ContactImportResult {
    object FileReadingFailed : ContactImportResult
    object VcfParsingFailed : ContactImportResult
    data class Success(val successfulContacts: List<IContact>, val numberOfFailedContacts: Int) : ContactImportResult
}
