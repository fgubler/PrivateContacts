package ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.mapping

import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.contact.ContactAccount
import ch.abwesend.privatecontacts.domain.model.contact.ContactBase
import ch.abwesend.privatecontacts.domain.model.contact.ContactEditable
import ch.abwesend.privatecontacts.domain.model.contact.ContactIdAndroid
import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.model.contact.IContact
import ch.abwesend.privatecontacts.domain.model.contact.IContactBase
import ch.abwesend.privatecontacts.domain.model.contact.IContactEditable
import ch.abwesend.privatecontacts.domain.model.contactdata.Company
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataCategory
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import com.alexstyl.contactstore.Contact
import com.alexstyl.contactstore.ContactGroup

class AndroidContactMapper {
    private val contactDataFactory: AndroidContactDataMapper by injectAnywhere()

    fun toContactBase(contact: Contact, rethrowExceptions: Boolean): IContactBase? =
        try {
            ContactBase(
                id = ContactIdAndroid(contactNo = contact.contactId),
                type = ContactType.PUBLIC,
                displayName = contact.displayName,
            )
        } catch (t: Throwable) {
            logger.warning("Failed to map android contact with id = ${contact.contactId}", t)
            if (rethrowExceptions) throw t
            else null
        }

    fun toContact(
        contact: Contact,
        groups: List<ContactGroup>,
        rethrowExceptions: Boolean
    ): IContact? = with(contact) {
        try {
            val middleNamePart = if (middleName.isBlank()) "" else " $middleName"
            ContactEditable(
                id = ContactIdAndroid(contactNo = contactId),
                importId = null,
                type = ContactType.PUBLIC,
                firstName = "$firstName$middleNamePart",
                lastName = lastName,
                nickname = nickname,
                middleName = middleName,
                namePrefix = prefix,
                nameSuffix = suffix,
                notes = note?.raw.orEmpty(),
                image = getImage(),
                contactDataSet = contactDataFactory.getContactData(contact).toMutableList(),
                contactGroups = groups.toContactGroups().toMutableList(),
                saveInAccount = ContactAccount.None,
            ).also { it.addCompanyWorkarounds(this) }
        } catch (t: Throwable) {
            logger.warning("Failed to map android contact with id = $contactId", t)
            if (rethrowExceptions) throw t
            else null
        }
    }

    /**
     * Contacts without first-/last-name are not allowed.
     * Workaround for nickname but also companies.
     * TODO consider removing this (partially?) once proper company-support is added
     */
    private fun IContactEditable.addCompanyWorkarounds(androidContact: Contact) {
        if (firstName.isEmpty() && lastName.isEmpty()) {
            if (nickname.isNotEmpty()) {
                firstName = nickname
            } else if (androidContact.organization.isNotEmpty()) {
                // temporary workaround: this should actually be stored in a field like "organizationName"
                firstName = androidContact.organization
            }
        }

        val numberOfCompanies = contactDataSet.count { it is Company }
        val companyFromOrganization = androidContact.organization
        val alreadyPresent = contactDataSet
            .any { it.category == ContactDataCategory.COMPANY && it.value == companyFromOrganization }

        if (companyFromOrganization.isNotEmpty() && !alreadyPresent) {
            val companyName = with(androidContact) {
                if (jobTitle.isBlank()) organization else "$organization ($jobTitle)"
            }
            val companyFromOrganisation = Company.createEmpty(sortOrder = numberOfCompanies)
                .copy(value = companyName, type = ContactDataType.Main)
            contactDataSet.add(companyFromOrganisation)
        }
    }
}
