/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository.vcard.service

import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.model.contact.IContact
import ch.abwesend.privatecontacts.domain.model.result.ContactExportResult
import ch.abwesend.privatecontacts.domain.model.result.ContactImportResult
import ch.abwesend.privatecontacts.domain.service.interfaces.IContactImportExportService
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import ch.abwesend.privatecontacts.infrastructure.repository.vcard.mapping.ContactToVCardMapper
import ch.abwesend.privatecontacts.infrastructure.repository.vcard.mapping.VCardToContactMapper
import ch.abwesend.privatecontacts.infrastructure.repository.vcard.repository.VCardRepository
import ezvcard.VCard
import java.io.File

// TODO add unit tests
class VCardImportExportService : IContactImportExportService {
    private val repository: VCardRepository by injectAnywhere()
    private val toVCardMapper: ContactToVCardMapper by injectAnywhere()
    private val fromVCardMapper: VCardToContactMapper by injectAnywhere()

    override suspend fun exportContacts(contacts: List<IContact>, targetFile: File): ContactExportResult {
        val vCards: List<VCard> = contacts.map { toVCardMapper.mapToVCard(it) }

        return try {
            repository.exportVCards(vCards, targetFile)
            ContactExportResult.Success(numberOfContacts = vCards.size)
        } catch (e: Exception) {
            logger.error("Failed to export vcf file", e)
            ContactExportResult.FileExportFailed(exception = e)
        }
    }

    override suspend fun importContacts(fileContent: List<String>, targetType: ContactType): ContactImportResult {
        val vCards = try {
            repository.importVCards(fileContent)
        } catch (e: Exception) {
            logger.error("Failed to import vcf file", e)
            // TODO fix data access permission problem
            return ContactImportResult.FileImportFailed(exception = e)
        }

        val contacts = vCards.map { fromVCardMapper.mapToContact(it) }

        return ContactImportResult.Success(numberOfContacts = contacts.size)
    }
}
