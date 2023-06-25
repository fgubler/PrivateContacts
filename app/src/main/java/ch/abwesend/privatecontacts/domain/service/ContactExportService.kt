/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.service

import android.net.Uri
import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.model.importexport.ContactExportData
import ch.abwesend.privatecontacts.domain.model.importexport.VCardCreateError
import ch.abwesend.privatecontacts.domain.model.result.generic.BinaryResult
import ch.abwesend.privatecontacts.domain.model.result.generic.SuccessResult

// TODO add unit tests
class ContactExportService {
    suspend fun exportContacts(
        targetFile: Uri?,
        sourceType: ContactType
    ): BinaryResult<ContactExportData, VCardCreateError> {
        // TODO implement

        val exportData = ContactExportData(successfullyExportedContacts = emptyList())
        return SuccessResult(exportData)
    }
}
