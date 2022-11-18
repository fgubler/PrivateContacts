package ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.repository

import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.ModelStatus
import ch.abwesend.privatecontacts.domain.model.contact.IContact
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactData
import ch.abwesend.privatecontacts.domain.model.contactdata.PhoneNumber
import ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.factory.toLabel
import com.alexstyl.contactstore.ImageData
import com.alexstyl.contactstore.MutableContact
import com.alexstyl.contactstore.Note
import com.alexstyl.contactstore.SaveRequest

fun MutableContact.updateChangedBaseData(originalContact: IContact, changedContact: IContact) {
    if (originalContact.firstName != changedContact.firstName) {
        firstName = changedContact.firstName
    }
    if (originalContact.lastName != changedContact.lastName) {
        lastName = changedContact.lastName
    }
    if (originalContact.nickname != changedContact.nickname) {
        nickname = changedContact.nickname
    }
    if (originalContact.notes != changedContact.notes) {
        note = Note(changedContact.notes)
    }
}

/**
 * Beware: untested (the UI cannot change images yet)
 * TODO test
 */
fun MutableContact.updateChangedImage(changedContact: IContact) {
    val newImage = changedContact.image

    when (newImage.modelStatus) {
        ModelStatus.UNCHANGED -> Unit
        ModelStatus.DELETED -> imageData = null
        ModelStatus.CHANGED, ModelStatus.NEW -> {
            // thumbnailUri seemingly cannot be changed
            imageData = newImage.fullImage?.let { ImageData(it) }
        }
    }
}

fun SaveRequest.updateContactGroups(changedContact: IContact) {
    // TODO not yet implemented
}

fun MutableContact.updateChangedContactData(changedContact: IContact) {
    updatePhoneNumbers(changedContact.contactDataSet)
    // TODO implement the others
}

private fun MutableContact.updatePhoneNumbers(contactData: List<ContactData>) {
    val oldPhoneNumbers = phones.toList()
    val newPhoneNumbers = contactData.filterIsInstance<PhoneNumber>()

    if (newPhoneNumbers.all { it.modelStatus == ModelStatus.UNCHANGED }) {
        logger.debug("No phone-numbers to change")
    } else {
        logger.debug("Some phone-numbers were changed, added or deleted")

        newPhoneNumbers.filter { it.modelStatus == ModelStatus.NEW }.forEach { newNumber ->
            val label = newNumber.type.toLabel(newNumber.category)

            // TODO continue implementing
//            val transformedNumber = LabeledValue
//            phones.add()
        }

        if (newPhoneNumbers.none { it.modelStatus == ModelStatus.CHANGED }) {
            logger.debug("Some phone-numbers were changed: need to merge")
            // TODO implement: the problem is to detect which ones were changed (exclusion principle?)
        }
    }
}
