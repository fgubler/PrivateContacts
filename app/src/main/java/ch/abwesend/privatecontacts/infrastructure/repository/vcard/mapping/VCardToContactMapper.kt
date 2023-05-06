/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository.vcard.mapping

import ch.abwesend.privatecontacts.domain.model.contact.ContactEditable
import ch.abwesend.privatecontacts.domain.model.contact.ContactIdInternal
import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.model.contact.IContact
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactData
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.Anniversary
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.Birthday
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import ch.abwesend.privatecontacts.infrastructure.repository.vcard.mapping.contactdata.ToPhysicalAddressMapper
import ch.abwesend.privatecontacts.infrastructure.repository.vcard.mapping.contactdata.toContactData
import ch.abwesend.privatecontacts.infrastructure.repository.vcard.mapping.contactdata.toContactGroup
import ezvcard.VCard

// TODO add unit tests
class VCardToContactMapper {
    private val addressMapper: ToPhysicalAddressMapper by injectAnywhere()

    fun mapToContact(vCard: VCard, targetType: ContactType): IContact {
        val uuid = vCard.uid?.toUuidOrNull()
        val contactId = uuid?.let { ContactIdInternal(uuid = it) } ?: ContactIdInternal.randomId()
        val contact = ContactEditable.createNew(id = contactId)
        contact.type = targetType

        contact.firstName = vCard.structuredName?.given ?: vCard.formattedName?.value.orEmpty()
        contact.lastName = vCard.structuredName?.family.orEmpty()

        val nicknames = vCard.nickname?.values ?: vCard.structuredName?.additionalNames.orEmpty()
        contact.nickname = nicknames.filterNotNull().joinToString(", ")

        val contactData = getContactData(vCard)
        contact.contactDataSet.addAll(contactData)

        // TODO test with a dataset which actually has categories
        val groups = vCard.categoriesList.orEmpty().mapNotNull { it.toContactGroup() }
        contact.contactGroups.addAll(groups)

        val contactCategory = vCard.kind // TODO use once this becomes a thing
        // TODO figure out how to handle the name of companies/organizations

        return contact
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

        // TODO get companies with some fancy mapping

        val allData = phoneNumbers + emails + addresses + websites + relationships + birthDays + anniversaries
        return allData.distinct()
    }
}
