/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.service.interfaces

import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.model.contact.IContact
import ch.abwesend.privatecontacts.domain.model.result.ContactExportResult
import ch.abwesend.privatecontacts.domain.model.result.ContactImportResult
import java.io.File

interface IContactImportExportService {
    suspend fun exportContacts(contacts: List<IContact>, targetFile: File): ContactExportResult
    suspend fun importContacts(fileContent: List<String>, targetType: ContactType): ContactImportResult
}
