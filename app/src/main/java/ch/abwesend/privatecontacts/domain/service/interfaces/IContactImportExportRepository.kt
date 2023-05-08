/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.service.interfaces

import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.model.contact.IContact
import ch.abwesend.privatecontacts.domain.model.importexport.FileContent
import ch.abwesend.privatecontacts.domain.model.result.ContactExportResult
import ch.abwesend.privatecontacts.domain.model.result.ContactImportResult

interface IContactImportExportRepository {
    suspend fun exportContacts(contacts: List<IContact>): ContactExportResult
    suspend fun loadContacts(fileContent: FileContent, targetType: ContactType): ContactImportResult
}
