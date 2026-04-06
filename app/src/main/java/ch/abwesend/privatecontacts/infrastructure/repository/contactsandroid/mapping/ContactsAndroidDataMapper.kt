/*
 * Private Contacts
 * Copyright (c) 2026.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository.contactsandroid.mapping

import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.ModelStatus
import ch.abwesend.privatecontacts.domain.model.contactdata.Company
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactData
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataIdAndroid
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType
import ch.abwesend.privatecontacts.domain.model.contactdata.EmailAddress
import ch.abwesend.privatecontacts.domain.model.contactdata.EventDate
import ch.abwesend.privatecontacts.domain.model.contactdata.IContactDataIdExternal
import ch.abwesend.privatecontacts.domain.model.contactdata.PhoneNumber
import ch.abwesend.privatecontacts.domain.model.contactdata.PhysicalAddress
import ch.abwesend.privatecontacts.domain.model.contactdata.Relationship
import ch.abwesend.privatecontacts.domain.model.contactdata.Website
import ch.abwesend.privatecontacts.domain.model.contactdata.createExternalDummyContactDataId
import ch.abwesend.privatecontacts.domain.service.interfaces.IAddressFormattingService
import ch.abwesend.privatecontacts.domain.service.interfaces.TelephoneService
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import ch.abwesend.privatecontacts.domain.util.sanitize
import ch.abwesend.privatecontacts.domain.util.sanitizePhoneNumbers
import ch.abwesend.privatecontacts.infrastructure.service.AndroidContactCompanyMappingService
import contacts.core.entities.Contact
import contacts.core.entities.Entity
import contacts.core.entities.Relation
import contacts.core.entities.RelationEntity

class ContactsAndroidDataMapper {
    private val telephoneService: TelephoneService by injectAnywhere()
    private val addressFormattingService: IAddressFormattingService by injectAnywhere()
    private val companyMappingService: AndroidContactCompanyMappingService by injectAnywhere()

    fun getContactData(contact: Contact): List<ContactData> {
        val rawContact = contact.rawContacts.firstOrNull() ?: return emptyList()
        return getPhoneNumbers(rawContact.phones) +
            getEmailAddresses(rawContact.emails) +
            getPhysicalAddresses(rawContact.addresses) +
            getWebsites(rawContact.websites) +
            getRelationships(rawContact.relations) +
            getEventDates(rawContact.events) +
            getCompanies(rawContact.relations, rawContact.organization)
    }

    fun getContactPhoneNumbers(contact: Contact): List<PhoneNumber> {
        val rawContact = contact.rawContacts.firstOrNull() ?: return emptyList()
        return getPhoneNumbers(rawContact.phones)
    }

    private fun getPhoneNumbers(phones: List<contacts.core.entities.Phone>): List<PhoneNumber> =
        phones.mapIndexed { index, phone ->
            val contactDataId = phone.toContactDataId()
            val type = phone.type.toContactDataType(phone.label)
            val number = phone.number.orEmpty()

            PhoneNumber(
                id = contactDataId,
                sortOrder = index,
                type = type,
                value = number,
                formattedValue = telephoneService.formatPhoneNumberForDisplay(number),
                valueForMatching = telephoneService.formatPhoneNumberForMatching(number),
                isMain = phone.isPrimary || phone.isSuperPrimary,
                modelStatus = ModelStatus.UNCHANGED,
            )
        }.sanitizePhoneNumbers()

    private fun getEmailAddresses(emails: List<contacts.core.entities.Email>): List<EmailAddress> =
        emails.mapIndexed { index, email ->
            val contactDataId = email.toContactDataId()
            val type = email.type.toContactDataType(email.label)

            EmailAddress(
                id = contactDataId,
                sortOrder = index,
                type = type,
                value = email.address.orEmpty(),
                isMain = email.isPrimary || email.isSuperPrimary,
                modelStatus = ModelStatus.UNCHANGED,
            )
        }.sanitize()

    private fun getPhysicalAddresses(addresses: List<contacts.core.entities.Address>): List<PhysicalAddress> =
        addresses.mapIndexed { index, address ->
            val contactDataId = address.toContactDataId()
            val type = address.type.toContactDataType(address.label)

            val completeAddress = addressFormattingService.formatAddress(
                street = address.street.orEmpty(),
                neighborhood = address.neighborhood.orEmpty(),
                city = address.city.orEmpty(),
                postalCode = address.postcode.orEmpty(),
                region = address.region.orEmpty(),
                country = address.country.orEmpty(),
            )

            PhysicalAddress(
                id = contactDataId,
                sortOrder = index,
                type = type,
                value = completeAddress,
                isMain = address.isPrimary || address.isSuperPrimary,
                modelStatus = ModelStatus.UNCHANGED,
            )
        }.sanitize()

    private fun getWebsites(websites: List<contacts.core.entities.Website>): List<Website> =
        websites.mapIndexed { index, website ->
            val contactDataId = website.toContactDataId()

            Website(
                id = contactDataId,
                sortOrder = index,
                type = ContactDataType.Main,
                value = website.url.orEmpty(),
                isMain = website.isPrimary || website.isSuperPrimary,
                modelStatus = ModelStatus.UNCHANGED,
            )
        }.sanitize()

    private fun getRelationships(relations: List<Relation>): List<Relationship> =
        relations
            .filterNot { isPseudoRelationForCompany(it) }
            .mapIndexed { index, relation ->
                val contactDataId = relation.toContactDataId()
                val type = relation.type.toContactDataType(relation.label)

                Relationship(
                    id = contactDataId,
                    sortOrder = index,
                    type = type,
                    value = relation.name.orEmpty(),
                    isMain = relation.isPrimary || relation.isSuperPrimary,
                    modelStatus = ModelStatus.UNCHANGED,
                )
            }.sanitize()

    private fun getEventDates(events: List<contacts.core.entities.Event>): List<EventDate> =
        events.mapIndexed { index, event ->
            val contactDataId = event.toContactDataId()
            val type = event.type.toContactDataType(event.label)

            val date = event.date?.let {
                EventDate.createDate(day = it.dayOfMonth, month = it.month, year = it.year)
            }

            EventDate(
                id = contactDataId,
                sortOrder = index,
                type = type,
                value = date,
                isMain = event.isPrimary || event.isSuperPrimary,
                modelStatus = ModelStatus.UNCHANGED,
            )
        }.sanitize()

    private fun getCompanies(
        relations: List<Relation>,
        organization: contacts.core.entities.Organization?,
    ): List<Company> {
        val companiesFromRelations = relations
            .filter { isPseudoRelationForCompany(it) }
            .mapIndexed { index, relation ->
                val contactDataId = relation.toContactDataId()
                val label = relation.label
                val type = label?.let { companyMappingService.decodeFromPseudoRelationshipLabel(it) }
                    ?: ContactDataType.Business

                Company(
                    id = contactDataId,
                    sortOrder = index,
                    type = type,
                    value = relation.name.orEmpty(),
                    isMain = relation.isPrimary || relation.isSuperPrimary,
                    modelStatus = ModelStatus.UNCHANGED,
                )
            }

        val companyFromOrganization = organization?.let { org ->
            val baseName = org.company.orEmpty()
            val department = org.department.orEmpty()
            val jobTitle = org.title.orEmpty()
            val companyName = when {
                jobTitle.isBlank() && department.isBlank() -> baseName
                jobTitle.isBlank() -> "baseName - $department"
                department.isBlank() -> "$baseName ($jobTitle)"
                else -> "$baseName - $department ($jobTitle)"
            }
            Company(
                id = org.toContactDataId(),
                sortOrder = companiesFromRelations.size,
                type = ContactDataType.Main,
                value = companyName,
                isMain = org.isPrimary || org.isSuperPrimary,
                modelStatus = ModelStatus.UNCHANGED,
            )
        }

        return (companiesFromRelations + listOfNotNull(companyFromOrganization)).sanitize()
    }

    private fun isPseudoRelationForCompany(relation: Relation): Boolean {
        val label = relation.label
        return relation.type == RelationEntity.Type.CUSTOM &&
            label != null &&
            companyMappingService.matchesCompanyCustomRelationshipPattern(label)
    }

    private fun Entity.toContactDataId(): IContactDataIdExternal =
        idOrNull?.let { dataId -> ContactDataIdAndroid(contactDataNo = dataId) }
            ?: createExternalDummyContactDataId().also {
                logger.debug("No ID found for contact data entity.")
            }
}
