/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository.vcard.mapping

import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.contact.ContactEditable
import ch.abwesend.privatecontacts.domain.model.contact.ContactImportId
import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.model.contact.IContactEditable
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactData
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.Anniversary
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.Birthday
import ch.abwesend.privatecontacts.domain.model.result.generic.BinaryResult
import ch.abwesend.privatecontacts.domain.model.result.generic.ErrorResult
import ch.abwesend.privatecontacts.domain.model.result.generic.SuccessResult
import ch.abwesend.privatecontacts.domain.service.ContactSanitizingService
import ch.abwesend.privatecontacts.domain.util.Constants
import ch.abwesend.privatecontacts.domain.util.enforceContinuousSortOrder
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import ch.abwesend.privatecontacts.domain.util.removeDuplicates
import ch.abwesend.privatecontacts.domain.util.removePhoneNumberDuplicates
import ch.abwesend.privatecontacts.infrastructure.repository.vcard.mapping.contactdata.import.ToPhysicalAddressMapper
import ch.abwesend.privatecontacts.infrastructure.repository.vcard.mapping.contactdata.import.firstTypeOrNull
import ch.abwesend.privatecontacts.infrastructure.repository.vcard.mapping.contactdata.import.toCompany
import ch.abwesend.privatecontacts.infrastructure.repository.vcard.mapping.contactdata.import.toContactData
import ch.abwesend.privatecontacts.infrastructure.repository.vcard.mapping.contactdata.import.toContactGroups
import ch.abwesend.privatecontacts.infrastructure.repository.vcard.mapping.contactdata.import.toContactImage
import ch.abwesend.privatecontacts.infrastructure.repository.vcard.mapping.contactdata.import.toRelationship
import ch.abwesend.privatecontacts.infrastructure.service.AndroidContactCompanyMappingService
import ezvcard.VCard
import ezvcard.property.Related

class VCardToContactMapper {
    private val addressMapper: ToPhysicalAddressMapper by injectAnywhere()
    private val companyMappingService: AndroidContactCompanyMappingService by injectAnywhere()
    private val sanitizingService: ContactSanitizingService by injectAnywhere()

    fun mapToContact(vCard: VCard, targetType: ContactType): BinaryResult<IContactEditable, Unit> =
        try {
            val vCardUuid = vCard.uid?.toUuidOrNull()
            val contactImportId = vCardUuid?.let { ContactImportId(it) }
            val contact = ContactEditable.createNew(importId = contactImportId)
            contact.type = targetType

            contact.firstName = vCard.structuredName?.given ?: vCard.formattedName?.value.orEmpty()
            contact.lastName = vCard.structuredName?.family.orEmpty()

            val nicknames = vCard.nickname?.values ?: vCard.structuredName?.additionalNames.orEmpty()
            contact.nickname = nicknames.filterNotNull().joinToString(", ")

            contact.notes = vCard.notes.orEmpty().mapNotNull { it.value }.joinToString(Constants.linebreak)

            val contactData = getContactData(vCard)
            contact.contactDataSet.addAll(contactData)

            val groups = vCard.categoriesList.orEmpty().flatMap { it.toContactGroups() }
            contact.contactGroups.addAll(groups)

            val image = vCard.photos.orEmpty().filterNotNull().toContactImage()
            contact.image = image

            val contactCategory = vCard.kind // TODO use once this becomes a thing
            // TODO figure out how to handle the name of companies/organizations

            SuccessResult(contact)
        } catch (e: Exception) {
            logger.warning("Failed to map contact '${vCard.uid}'")
            ErrorResult(Unit)
        }

    private fun getContactData(vCard: VCard): List<ContactData> {
        val phoneNumbers = vCard.telephoneNumbers.orEmpty()
            .filterNotNull()
            .mapIndexedNotNull { index, elem -> elem.toContactData(index) }
            .map { sanitizingService.sanitizePhoneNumber(it) }
            .removePhoneNumberDuplicates()
            .enforceContinuousSortOrder()
        val emails = vCard.emails.orEmpty()
            .filterNotNull()
            .mapIndexedNotNull { index, elem -> elem.toContactData(index) }
            .removeDuplicates()
            .enforceContinuousSortOrder()
        val addresses = vCard.addresses.orEmpty()
            .filterNotNull()
            .mapIndexedNotNull { index, elem -> addressMapper.toContactData(elem, index) }
            .removeDuplicates()
            .enforceContinuousSortOrder()
        val websites = vCard.urls.orEmpty()
            .filterNotNull()
            .mapIndexedNotNull { index, elem -> elem.toContactData(index) }
            .removeDuplicates()
            .enforceContinuousSortOrder()

        val relationships = vCard.relations.orEmpty()
            .filterNotNull()
            .filterNot { it.isPseudoRelationForCompany() }
            .mapIndexedNotNull { index, elem -> elem.toRelationship(index) }
            .removeDuplicates()
            .enforceContinuousSortOrder()

        val companiesFromRelations = vCard.relations.orEmpty() // companies from pseudo-relations
            .filterNotNull()
            .filter { it.isPseudoRelationForCompany() }
            .mapIndexedNotNull { index, elem -> elem.toCompany(index, companyMappingService) }
        val numberOfRelationCompanies = companiesFromRelations.size
        val companiesFromOrganisations = vCard.organizations // companies from organizations
            .filterNotNull()
            .mapIndexedNotNull { index, elem -> elem.toCompany(numberOfRelationCompanies + index) }
        val allCompanies = (companiesFromRelations + companiesFromOrganisations)
            .removeDuplicates()
            .enforceContinuousSortOrder()

        val birthDays = vCard.birthdays.orEmpty()
            .filterNotNull()
            .mapIndexedNotNull { index, elem -> elem.toContactData(Birthday, index) }
            .removeDuplicates()
            .enforceContinuousSortOrder()
        val anniversaries = vCard.anniversaries.orEmpty()
            .filterNotNull()
            .mapIndexedNotNull { index, elem -> elem.toContactData(Anniversary, index) }
            .removeDuplicates()
            .enforceContinuousSortOrder()

        val allData = phoneNumbers + emails + addresses + websites +
            relationships + allCompanies + birthDays + anniversaries
        return allData.distinct()
    }

    private fun Related.isPseudoRelationForCompany(): Boolean {
        val type = firstTypeOrNull
        return type != null && companyMappingService.matchesCompanyCustomRelationshipPattern(type)
    }
}
