/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository.vcard.repository

import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.model.contact.IContact
import ch.abwesend.privatecontacts.domain.model.importexport.ContactImportPartialData.ParsedData
import ch.abwesend.privatecontacts.domain.model.importexport.FileContent
import ch.abwesend.privatecontacts.domain.model.importexport.VCardParseError.VCF_PARSING_FAILED
import ch.abwesend.privatecontacts.domain.model.result.ContactExportResult
import ch.abwesend.privatecontacts.domain.model.result.generic.ErrorResult
import ch.abwesend.privatecontacts.domain.model.result.generic.SuccessResult
import ch.abwesend.privatecontacts.domain.service.interfaces.ContactParseResult
import ch.abwesend.privatecontacts.domain.service.interfaces.IVCardImportExportRepository
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import ch.abwesend.privatecontacts.infrastructure.repository.vcard.mapping.ContactToVCardMapper
import ch.abwesend.privatecontacts.infrastructure.repository.vcard.mapping.VCardToContactMapper
import ezvcard.VCard

// TODO add unit tests
class VCardImportExportRepository : IVCardImportExportRepository {
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
            return ErrorResult(VCF_PARSING_FAILED)
        }

        val contacts = vCards.map { fromVCardMapper.mapToContact(it, targetType) }
        val successfulContacts = contacts.mapNotNull { it.getValueOrNull() }
        val result = ParsedData(
            successfulContacts = successfulContacts,
            numberOfFailedContacts = contacts.size - successfulContacts.size,
        )
        return SuccessResult(result)
    }
}
