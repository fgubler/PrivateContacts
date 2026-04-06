/*
 * Private Contacts
 * Copyright (c) 2026.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository.contactsandroid.mapping

import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.ModelStatus.UNCHANGED
import ch.abwesend.privatecontacts.domain.model.contact.ContactAccount
import ch.abwesend.privatecontacts.domain.model.contact.ContactBase
import ch.abwesend.privatecontacts.domain.model.contact.ContactEditable
import ch.abwesend.privatecontacts.domain.model.contact.ContactIdAndroid
import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.model.contact.ContactWithPhoneNumbers
import ch.abwesend.privatecontacts.domain.model.contact.IContact
import ch.abwesend.privatecontacts.domain.model.contact.IContactBase
import ch.abwesend.privatecontacts.domain.model.contact.IContactEditable
import ch.abwesend.privatecontacts.domain.model.contactdata.Company
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataCategory
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType
import ch.abwesend.privatecontacts.domain.model.contactdata.PhoneNumberValue
import ch.abwesend.privatecontacts.domain.model.contactgroup.ContactGroup
import ch.abwesend.privatecontacts.domain.model.contactimage.ContactImage
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import contacts.core.Contacts
import contacts.core.entities.Contact
import contacts.core.util.photoBytes

class ContactsAndroidContactMapper {
    private val contactDataMapper: ContactsAndroidDataMapper by injectAnywhere()

    fun toContactBase(contact: Contact, rethrowExceptions: Boolean): IContactBase? =
        try {
            ContactBase(
                id = ContactIdAndroid(
                    contactNo = contact.id,
                    lookupKey = contact.lookupKey,
                ),
                type = ContactType.PUBLIC,
                displayName = contact.displayNamePrimary.orEmpty(),
            )
        } catch (t: Throwable) {
            logger.warning("Failed to map contacts-android contact with id = ${contact.id}", t)
            if (rethrowExceptions) throw t
            else null
        }

    fun toContactWithPhoneNumbers(contact: Contact, rethrowExceptions: Boolean): ContactWithPhoneNumbers? =
        try {
            val contactBase = toContactBase(contact = contact, rethrowExceptions = rethrowExceptions)
            val phoneNumbers = contactDataMapper.getContactPhoneNumbers(contact)
                .map { PhoneNumberValue(it.value) }
            contactBase?.let { ContactWithPhoneNumbers(contactBase, phoneNumbers) }
        } catch (t: Throwable) {
            logger.warning("Failed to map contacts-android contact with id = ${contact.id}", t)
            if (rethrowExceptions) throw t
            else null
        }

    fun toContact(
        contact: Contact,
        groups: List<ContactGroup>,
        rethrowExceptions: Boolean,
    ): IContact? {
        try {
            val rawContact = contact.rawContacts.firstOrNull()
            val name = rawContact?.name

            val image = getImage(contact)

            val editable = ContactEditable(
                id = ContactIdAndroid(
                    contactNo = contact.id,
                    lookupKey = contact.lookupKey,
                ),
                importId = null,
                type = ContactType.PUBLIC,
                firstName = name?.givenName.orEmpty(),
                lastName = name?.familyName.orEmpty(),
                nickname = rawContact?.nickname?.name.orEmpty(),
                middleName = name?.middleName.orEmpty(),
                namePrefix = name?.prefix.orEmpty(),
                nameSuffix = name?.suffix.orEmpty(),
                notes = rawContact?.note?.note.orEmpty(),
                image = image,
                contactDataSet = contactDataMapper.getContactData(contact).toMutableList(),
                contactGroups = groups.toMutableList(),
                saveInAccount = ContactAccount.None,
            )
            editable.addCompanyWorkarounds(rawContact?.organization)
            return editable
        } catch (t: Throwable) {
            logger.warning("Failed to map contacts-android contact with id = ${contact.id}", t)
            if (rethrowExceptions) throw t
            else return null
        }
    }

    private fun getImage(contact: Contact): ContactImage {
        val thumbnailUriString = try {
            contact.photoThumbnailUri?.toString()
        } catch (e: Exception) {
            logger.warning("Failed to get thumbnailUri for contact ${contact.id}", e)
            null
        }
        val fullImageBytes = try {
            val contactsApi: Contacts by injectAnywhere()
            contact.photoBytes(contactsApi)
        } catch (e: Exception) {
            logger.warning("Failed to get photo bytes for contact ${contact.id}", e)
            null
        }
        return ContactImage(
            thumbnailUri = thumbnailUriString,
            fullImage = fullImageBytes,
            modelStatus = UNCHANGED,
        )
    }

    /**
     * Contacts without first-/last-name are not allowed.
     * Workaround for nickname but also companies.
     * Mirrors the same logic from the old AndroidContactMapper.
     */
    private fun IContactEditable.addCompanyWorkarounds(
        organization: contacts.core.entities.Organization?,
    ) {
        val orgName = organization?.company.orEmpty()
        val jobTitle = organization?.title.orEmpty()

        if (firstName.isEmpty() && lastName.isEmpty()) {
            if (nickname.isNotEmpty()) {
                firstName = nickname
            } else if (orgName.isNotEmpty()) {
                firstName = orgName
            }
        }

        val numberOfCompanies = contactDataSet.count { it is Company }
        val alreadyPresent = contactDataSet
            .any { it.category == ContactDataCategory.COMPANY && it.value == orgName }

        if (orgName.isNotEmpty() && !alreadyPresent) {
            val companyName = if (jobTitle.isBlank()) orgName else "$orgName ($jobTitle)"
            val companyFromOrganisation = Company.createEmpty(sortOrder = numberOfCompanies)
                .copy(value = companyName, type = ContactDataType.Main)
            contactDataSet.add(companyFromOrganisation)
        }
    }
}
