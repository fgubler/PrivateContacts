package ch.abwesend.privatecontacts.infrastructure.repository.vcard.mapping

import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.contact.IContact
import ch.abwesend.privatecontacts.domain.model.contact.IContactIdExternal
import ch.abwesend.privatecontacts.domain.model.contact.IContactIdInternal
import ch.abwesend.privatecontacts.domain.model.contactdata.Company
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType
import ch.abwesend.privatecontacts.domain.model.contactdata.EmailAddress
import ch.abwesend.privatecontacts.domain.model.contactdata.EventDate
import ch.abwesend.privatecontacts.domain.model.contactdata.PhoneNumber
import ch.abwesend.privatecontacts.domain.model.contactdata.PhysicalAddress
import ch.abwesend.privatecontacts.domain.model.contactdata.Relationship
import ch.abwesend.privatecontacts.domain.model.contactdata.Website
import ch.abwesend.privatecontacts.domain.model.result.generic.BinaryResult
import ch.abwesend.privatecontacts.domain.model.result.generic.ErrorResult
import ch.abwesend.privatecontacts.domain.model.result.generic.SuccessResult
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import ch.abwesend.privatecontacts.infrastructure.repository.vcard.mapping.contactdata.export.toCategories
import ch.abwesend.privatecontacts.infrastructure.repository.vcard.mapping.contactdata.export.toVCardAddress
import ch.abwesend.privatecontacts.infrastructure.repository.vcard.mapping.contactdata.export.toVCardAnniversary
import ch.abwesend.privatecontacts.infrastructure.repository.vcard.mapping.contactdata.export.toVCardBirthday
import ch.abwesend.privatecontacts.infrastructure.repository.vcard.mapping.contactdata.export.toVCardCompany
import ch.abwesend.privatecontacts.infrastructure.repository.vcard.mapping.contactdata.export.toVCardEmailAddress
import ch.abwesend.privatecontacts.infrastructure.repository.vcard.mapping.contactdata.export.toVCardPhoneNumber
import ch.abwesend.privatecontacts.infrastructure.repository.vcard.mapping.contactdata.export.toVCardRelationship
import ch.abwesend.privatecontacts.infrastructure.repository.vcard.mapping.contactdata.export.toVCardUrl
import ch.abwesend.privatecontacts.infrastructure.service.AndroidContactCompanyMappingService
import ezvcard.VCard
import ezvcard.property.Kind
import ezvcard.property.Nickname
import ezvcard.property.Note
import ezvcard.property.StructuredName
import java.util.UUID

class ContactToVCardMapper {
    private val companyMappingService: AndroidContactCompanyMappingService by injectAnywhere()

    fun mapToVCard(contact: IContact): BinaryResult<VCard, IContact> =
        try {
            val vCard = VCard()

            val uuid = when (val contactId = contact.id) {
                is IContactIdInternal -> contactId.uuid
                is IContactIdExternal -> UUID.randomUUID()
            }

            vCard.uid = uuid.toUid()

            val structuredName = StructuredName()
            structuredName.given = contact.firstName
            structuredName.family = contact.lastName
            vCard.structuredName = structuredName

            val nickname = Nickname()
            nickname.values.add(contact.nickname)
            vCard.addNickname(nickname)

            val note = Note(contact.notes)
            vCard.addNote(note)

            vCard.addContactData(contact)

            val categories = contact.contactGroups.toCategories()
            vCard.addCategories(categories)

            vCard.kind = Kind.individual() // TODO compute once non-person contacts are supported
            SuccessResult(vCard)
        } catch (e: Exception) {
            logger.warning("Failed to map contact '${contact.id}'")
            ErrorResult(contact)
        }

    private fun VCard.addContactData(contact: IContact) {
        val phoneNumbers = contact.contactDataSet.filterIsInstance<PhoneNumber>()
        val vCardPhoneNumbers = phoneNumbers.sortedBy { it.sortOrder }.map { it.toVCardPhoneNumber() }
        telephoneNumbers.addAll(vCardPhoneNumbers)

        val emailAddresses = contact.contactDataSet.filterIsInstance<EmailAddress>()
        val vCardEmailAddresses = emailAddresses.sortedBy { it.sortOrder }.map { it.toVCardEmailAddress() }
        emails.addAll(vCardEmailAddresses)

        val physicalAddresses = contact.contactDataSet.filterIsInstance<PhysicalAddress>()
        val vCardPhysicalAddresses = physicalAddresses.sortedBy { it.sortOrder }.map { it.toVCardAddress() }
        addresses.addAll(vCardPhysicalAddresses)

        val websites = contact.contactDataSet.filterIsInstance<Website>()
        val vCardWebsites = websites.sortedBy { it.sortOrder }.map { it.toVCardUrl() }
        urls.addAll(vCardWebsites)

        val relationships = contact.contactDataSet.filterIsInstance<Relationship>()
        val vCardRelationships = relationships.sortedBy { it.sortOrder }.map { it.toVCardRelationship() }
        relations.addAll(vCardRelationships)

        val companies = contact.contactDataSet.filterIsInstance<Company>()
        val vCardCompanies = companies.sortedBy { it.sortOrder }.map { it.toVCardCompany(companyMappingService) }
        relations.addAll(vCardCompanies)

        addEventDates(contact)
    }

    private fun VCard.addEventDates(contact: IContact) {
        val eventDates = contact.contactDataSet.filterIsInstance<EventDate>()
        val birthdayEvents = eventDates.filter { it.type == ContactDataType.Birthday }
        val vCardBirthdays = birthdayEvents.sortedBy { it.sortOrder }.map { it.toVCardBirthday() }
        birthdays.addAll(vCardBirthdays)

        val anniversaryEvents = eventDates.filter { it.type == ContactDataType.Anniversary }
        val vCardAnniversaries = anniversaryEvents.sortedBy { it.sortOrder }.map { it.toVCardAnniversary() }
        anniversaries.addAll(vCardAnniversaries)
    }
}
