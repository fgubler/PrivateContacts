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
import ch.abwesend.privatecontacts.domain.util.Constants
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import ch.abwesend.privatecontacts.infrastructure.repository.vcard.mapping.contactdata.import.ToPhysicalAddressMapper
import ch.abwesend.privatecontacts.infrastructure.repository.vcard.mapping.contactdata.import.toContactData
import ch.abwesend.privatecontacts.infrastructure.repository.vcard.mapping.contactdata.import.toContactGroups
import ezvcard.VCard

class VCardToContactMapper {
    private val addressMapper: ToPhysicalAddressMapper by injectAnywhere()

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

            val contactCategory = vCard.kind // TODO use once this becomes a thing
            // TODO figure out how to handle the name of companies/organizations

            SuccessResult(contact)
        } catch (e: Exception) {
            logger.warning("Failed to map contact '${vCard.uid}'")
            ErrorResult(Unit)
        }

    private fun getContactData(vCard: VCard): List<ContactData> {
        val phoneNumbers = vCard.telephoneNumbers.orEmpty()
            .mapIndexedNotNull { index, elem -> elem.toContactData(index) }
        val emails = vCard.emails.orEmpty()
            .mapIndexedNotNull { index, elem -> elem.toContactData(index) }
        val addresses = vCard.addresses.orEmpty()
            .mapIndexedNotNull { index, elem -> addressMapper.toContactData(elem, index) }
        val websites = vCard.urls.orEmpty()
            .mapIndexedNotNull { index, elem -> elem.toContactData(index) }
        val relationships = vCard.relations.orEmpty()
            .mapIndexedNotNull { index, elem -> elem.toContactData(index) }
        val birthDays = vCard.birthdays.orEmpty()
            .mapIndexedNotNull { index, elem -> elem.toContactData(Birthday, index) }
        val anniversaries = vCard.anniversaries.orEmpty()
            .mapIndexedNotNull { index, elem -> elem.toContactData(Anniversary, index) }

        // parsing companies from custom-relationships is useless: VCF somehow drops all relationships

        val allData = phoneNumbers + emails + addresses + websites + relationships + birthDays + anniversaries
        return allData.distinct()
    }
}
