/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.result

sealed interface ContactExportResult {
    data class FileExportFailed(val exception: Exception) : ContactExportResult
    data class Success(val numberOfContacts: Int) : ContactExportResult
}
