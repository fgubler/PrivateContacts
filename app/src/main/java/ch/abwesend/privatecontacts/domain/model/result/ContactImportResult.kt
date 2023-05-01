/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.result

sealed interface ContactImportResult {
    data class FileImportFailed(val exception: Exception) : ContactImportResult
    data class Success(val numberOfContacts: Int) : ContactImportResult
}
