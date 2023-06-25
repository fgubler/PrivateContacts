/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.service

import android.net.Uri
import ch.abwesend.privatecontacts.domain.lib.coroutine.IDispatchers
import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.model.importexport.ContactExportData
import ch.abwesend.privatecontacts.domain.model.importexport.VCardCreateError
import ch.abwesend.privatecontacts.domain.model.result.generic.BinaryResult
import ch.abwesend.privatecontacts.domain.model.result.generic.SuccessResult
import ch.abwesend.privatecontacts.domain.service.interfaces.IVCardImportExportRepository
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import kotlinx.coroutines.withContext

// TODO add unit tests
class ContactExportService {
    private val dispatchers: IDispatchers by injectAnywhere()
    private val loadService: ContactLoadService by injectAnywhere()
    private val importExportRepository: IVCardImportExportRepository by injectAnywhere()

    suspend fun exportContacts(
        targetFile: Uri?,
        sourceType: ContactType
    ): BinaryResult<ContactExportData, VCardCreateError> = withContext(dispatchers.default) {
        val contacts = loadService.loadFullContactsByType(sourceType)

        // TODO this repository should return the vcf file-content as string (with proper error-handling)
        val serializedVcfString = importExportRepository.exportContacts(contacts)
        // TODO write resulting string to file...

        // TODO return proper data
        val exportData = ContactExportData(successfullyExportedContacts = emptyList())
        SuccessResult(exportData)
    }
}
