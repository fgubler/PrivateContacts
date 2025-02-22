/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.importexport

import ch.abwesend.privatecontacts.domain.model.contact.IContact

data class ContactExportData(
    val successfulContacts: List<IContact>,
    val failedContacts: List<IContact>,
)

sealed interface ContactExportPartialData {
    data class CreatedVCards(
        val fileContent: TextFileContent,
        val failedContacts: List<IContact>
    ) : ContactExportPartialData
}
