/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository.vcard.repository

import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.model.contact.IContact
import ch.abwesend.privatecontacts.domain.model.importexport.ContactParseError.VCF_PARSING_FAILED
import ch.abwesend.privatecontacts.domain.model.importexport.ContactParsedData
import ch.abwesend.privatecontacts.domain.model.importexport.FileContent
import ch.abwesend.privatecontacts.domain.model.result.ContactExportResult
import ch.abwesend.privatecontacts.domain.model.result.Result
import ch.abwesend.privatecontacts.domain.service.interfaces.ContactParseResult
import ch.abwesend.privatecontacts.domain.service.interfaces.IContactImportExportRepository
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import ch.abwesend.privatecontacts.infrastructure.repository.vcard.mapping.ContactToVCardMapper
import ch.abwesend.privatecontacts.infrastructure.repository.vcard.mapping.VCardToContactMapper
import ezvcard.VCard

// TODO add unit tests
class VCardImportExportRepository : IContactImportExportRepository {
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

    override suspend fun parseContacts(fileContent: FileContent, targetType: ContactType): ContactParseResult {
        val vCards = try {
            repository.importVCards(fileContent)
        } catch (e: Exception) {
            logger.error("Failed to import vcf file", e)
            return Result.Error(VCF_PARSING_FAILED)
        }

        val contacts = vCards.map { fromVCardMapper.mapToContact(it, targetType) }
        val successfulContacts = contacts.mapNotNull { it.getValueOrNull() }
        val result = ContactParsedData(
            successfulContacts = successfulContacts,
            numberOfFailedContacts = contacts.size - successfulContacts.size,
        )
        return Result.Success(result)
    }
}
