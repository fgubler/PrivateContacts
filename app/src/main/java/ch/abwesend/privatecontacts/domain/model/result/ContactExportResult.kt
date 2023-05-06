/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.result

import ch.abwesend.privatecontacts.domain.model.importexport.FileContent

sealed interface ContactExportResult {
    data class VcfWritingFailed(val exception: Exception) : ContactExportResult
    data class Success(val fileContent: FileContent) : ContactExportResult
}
