/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository.vcard.service

import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.model.contact.IContact
import ch.abwesend.privatecontacts.domain.model.importexport.FileContent
import ch.abwesend.privatecontacts.domain.model.result.ContactExportResult
import ch.abwesend.privatecontacts.domain.model.result.ContactImportResult
import ch.abwesend.privatecontacts.domain.service.interfaces.IContactImportExportService
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import ch.abwesend.privatecontacts.infrastructure.repository.vcard.mapping.ContactToVCardMapper
import ch.abwesend.privatecontacts.infrastructure.repository.vcard.mapping.VCardToContactMapper
import ch.abwesend.privatecontacts.infrastructure.repository.vcard.repository.VCardRepository
import ezvcard.VCard

// TODO add unit tests
class VCardImportExportService : IContactImportExportService {
    private val repository: VCardRepository by injectAnywhere()
    private val toVCardMapper: ContactToVCardMapper by injectAnywhere()
    private val fromVCardMapper: VCardToContactMapper by injectAnywhere()

    override suspend fun exportContacts(contacts: List<IContact>): ContactExportResult {
        val vCards: List<VCard> = contacts.map { toVCardMapper.mapToVCard(it) }

        return try {
            val fileContent = repository.exportVCards(vCards)
            ContactExportResult.Success(fileContent = fileContent)
        } catch (e: Exception) {
            logger.error("Failed to export vcf file", e)
            ContactExportResult.VcfWritingFailed(exception = e)
        }
    }

    override suspend fun importContacts(fileContent: FileContent, targetType: ContactType): ContactImportResult {
        val vCards = try {
            repository.importVCards(fileContent)
        } catch (e: Exception) {
            logger.error("Failed to import vcf file", e)
            return ContactImportResult.VcfParsingFailed(exception = e)
        }

        val contacts = vCards.map { fromVCardMapper.mapToContact(it, targetType) }
        return ContactImportResult.Success(numberOfContacts = contacts.size)
    }
}
