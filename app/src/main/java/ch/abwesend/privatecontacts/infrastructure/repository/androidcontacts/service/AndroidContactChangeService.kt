/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.service

import android.net.Uri
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.ModelStatus
import ch.abwesend.privatecontacts.domain.model.ModelStatus.CHANGED
import ch.abwesend.privatecontacts.domain.model.ModelStatus.DELETED
import ch.abwesend.privatecontacts.domain.model.ModelStatus.NEW
import ch.abwesend.privatecontacts.domain.model.contact.IContact
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactData
import ch.abwesend.privatecontacts.domain.model.contactdata.EmailAddress
import ch.abwesend.privatecontacts.domain.model.contactdata.EventDate
import ch.abwesend.privatecontacts.domain.model.contactdata.IContactDataIdExternal
import ch.abwesend.privatecontacts.domain.model.contactdata.PhoneNumber
import ch.abwesend.privatecontacts.domain.model.contactdata.PhysicalAddress
import ch.abwesend.privatecontacts.domain.model.contactdata.Relationship
import ch.abwesend.privatecontacts.domain.model.contactdata.Website
import ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.factory.toLabel
import com.alexstyl.contactstore.ImageData
import com.alexstyl.contactstore.LabeledValue
import com.alexstyl.contactstore.MutableContact
import com.alexstyl.contactstore.Note
import com.alexstyl.contactstore.EventDate as ContactStoreEventDate
import com.alexstyl.contactstore.MailAddress as ContactStoreMailAddress
import com.alexstyl.contactstore.PhoneNumber as ContactStorePhoneNumber
import com.alexstyl.contactstore.PostalAddress as ContactStorePostalAddress
import com.alexstyl.contactstore.Relation as ContactStoreRelation
import com.alexstyl.contactstore.WebAddress as ContactStoreWebAddress

class AndroidContactChangeService {
    fun updateChangedBaseData(originalContact: IContact, changedContact: IContact, mutableContact: MutableContact) {
        if (originalContact.firstName != changedContact.firstName) {
            mutableContact.firstName = changedContact.firstName
        }
        if (originalContact.lastName != changedContact.lastName) {
            mutableContact.lastName = changedContact.lastName
        }
        if (originalContact.nickname != changedContact.nickname) {
            mutableContact.nickname = changedContact.nickname
        }
        if (originalContact.notes != changedContact.notes) {
            mutableContact.note = Note(changedContact.notes)
        }
    }

    /**
     * Beware: not yet tested manually (the UI cannot change images yet)
     * TODO test manually
     */
    fun updateChangedImage(changedContact: IContact, mutableContact: MutableContact) {
        val newImage = changedContact.image

        when (newImage.modelStatus) {
            ModelStatus.UNCHANGED -> Unit
            DELETED -> mutableContact.imageData = null
            CHANGED, NEW -> {
                // thumbnailUri seemingly cannot be changed
                mutableContact.imageData = newImage.fullImage?.let { ImageData(it) }
            }
        }
    }

    fun updateChangedContactData(changedContact: IContact, mutableContact: MutableContact) {
        mutableContact.updatePhoneNumbers(changedContact.contactDataSet)
        mutableContact.updateEmailAddresses(changedContact.contactDataSet)
        mutableContact.updatePhysicalAddresses(changedContact.contactDataSet)
        mutableContact.updateWebsites(changedContact.contactDataSet)
        mutableContact.updateRelationships(changedContact.contactDataSet)
        mutableContact.updateEventDates(changedContact.contactDataSet)
        // companies cannot be edited because Android only allows one while we allow several => a bit of a mess
    }

    private fun MutableContact.updatePhoneNumbers(contactData: List<ContactData>) {
        val phoneNumbers = contactData.filterIsInstance<PhoneNumber>()
        logger.debug("Updating ${phoneNumbers.size} phone numbers on contact $contactId")

        updateContactDataOfType(
            newContactData = phoneNumbers,
            mutableDataOnContact = phones,
        ) { newPhoneNumber -> ContactStorePhoneNumber(raw = newPhoneNumber.value) }
    }

    private fun MutableContact.updateEmailAddresses(contactData: List<ContactData>) {
        val emailAddresses = contactData.filterIsInstance<EmailAddress>()
        logger.debug("Updating ${emailAddresses.size} email addresses on contact $contactId")

        updateContactDataOfType(
            newContactData = emailAddresses,
            mutableDataOnContact = mails,
        ) { newEmailAddress -> ContactStoreMailAddress(raw = newEmailAddress.value) }
    }

    private fun MutableContact.updatePhysicalAddresses(contactData: List<ContactData>) {
        val addresses = contactData.filterIsInstance<PhysicalAddress>()
        logger.debug("Updating ${addresses.size} physical addresses on contact $contactId")

        updateContactDataOfType(
            newContactData = addresses,
            mutableDataOnContact = postalAddresses,
        ) { newAddress -> ContactStorePostalAddress(street = newAddress.value) }
    }

    /** Beware: Uri.parse() needs to be mocked in unit-tests */
    private fun MutableContact.updateWebsites(contactData: List<ContactData>) {
        val websites = contactData.filterIsInstance<Website>()
        logger.debug("Updating ${websites.size} websites on contact $contactId")

        updateContactDataOfType(
            newContactData = websites,
            mutableDataOnContact = webAddresses,
        ) { newWebsite ->
            val uri = runCatching { Uri.parse(newWebsite.value) }
                .onFailure { logger.warning("Failed to parse URI for website ${newWebsite.id}") }
                .getOrNull()
            uri?.let { ContactStoreWebAddress(raw = it) }
        }
    }

    private fun MutableContact.updateRelationships(contactData: List<ContactData>) {
        val relationships = contactData.filterIsInstance<Relationship>()
        logger.debug("Updating ${relationships.size} relationships on contact $contactId")

        updateContactDataOfType(
            newContactData = relationships,
            mutableDataOnContact = relations,
        ) { newRelationship -> ContactStoreRelation(name = newRelationship.value) }
    }

    private fun MutableContact.updateEventDates(contactData: List<ContactData>) {
        val eventDates = contactData.filterIsInstance<EventDate>()
        logger.debug("Updating ${eventDates.size} events on contact $contactId")

        updateContactDataOfType(
            newContactData = eventDates,
            mutableDataOnContact = events,
        ) { newDate ->
            newDate.value?.let { date ->
                ContactStoreEventDate(
                    dayOfMonth = date.dayOfMonth,
                    month = date.month.value,
                    year = date.year,
                )
            }
        }
    }

    private fun <TInternal : ContactData, TExternal : Any> updateContactDataOfType(
        newContactData: List<TInternal>,
        mutableDataOnContact: MutableList<LabeledValue<TExternal>>,
        mapper: (TInternal) -> TExternal?,
    ) {
        val contactDataCategory = newContactData.firstOrNull()?.javaClass?.simpleName ?: "[Unknown]"
        if (newContactData.all { it.modelStatus == ModelStatus.UNCHANGED }) {
            logger.debug("No contact-data of category $contactDataCategory to change")
            return
        }

        logger.debug("Some contact-data of category $contactDataCategory was changed, added or deleted")

        val oldContactDataByNo = mutableDataOnContact.associateBy { it.id }
        val contactDataToChange = newContactData.filter { it.modelStatus in listOf(NEW, CHANGED) }
        val contactDataToDelete = newContactData.filter { it.modelStatus == DELETED }

        contactDataToDelete.forEach { number -> mutableDataOnContact.deleteContactData(number, oldContactDataByNo) }

        contactDataToChange.forEach { newNumber ->
            mutableDataOnContact.upsertContactData(newNumber, oldContactDataByNo, mapper)
        }
    }

    private fun <TExternal : Any> MutableList<LabeledValue<TExternal>>.deleteContactData(
        contactData: ContactData,
        oldContactDataByNo: Map<Long?, LabeledValue<TExternal>>,
    ) {
        val contactDataId = contactData.idExternal
        val correspondingOldData = oldContactDataByNo[contactDataId?.contactDataNo]
        correspondingOldData?.let { remove(it) }
    }

    private fun <TInternal : ContactData, TExternal : Any> MutableList<LabeledValue<TExternal>>.upsertContactData(
        contactData: TInternal,
        oldContactDataByNo: Map<Long?, LabeledValue<TExternal>>,
        mapper: (TInternal) -> TExternal?,
    ) {
        val contactDataId = contactData.idExternal
        val correspondingOldData = oldContactDataByNo[contactDataId?.contactDataNo]
        val newLabel = contactData.type.toLabel(contactData.category, correspondingOldData?.label)

        val mappedValue = mapper(contactData)

        mappedValue?.let {
            val transformedNewData = LabeledValue(value = it, label = newLabel)

            val indexOfOldNumber = indexOf(correspondingOldData)
            if (indexOfOldNumber >= 0) {
                this[indexOfOldNumber] = transformedNewData
            } else {
                add(transformedNewData)
            }
        } ?: logger.info("Mapper returned null for contact-data $contactDataId => ignoring it")
    }

    private val ContactData.idExternal: IContactDataIdExternal?
        get() = (id as? IContactDataIdExternal)
            .also { if (it == null) logger.warning("Phone number should have an external ID: $id") }
}
