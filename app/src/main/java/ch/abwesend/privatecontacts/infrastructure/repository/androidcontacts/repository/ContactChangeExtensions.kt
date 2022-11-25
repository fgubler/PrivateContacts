package ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.repository

import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.ModelStatus
import ch.abwesend.privatecontacts.domain.model.ModelStatus.CHANGED
import ch.abwesend.privatecontacts.domain.model.ModelStatus.DELETED
import ch.abwesend.privatecontacts.domain.model.ModelStatus.NEW
import ch.abwesend.privatecontacts.domain.model.contact.IContact
import ch.abwesend.privatecontacts.domain.model.contactdata.BaseGenericContactData
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactData
import ch.abwesend.privatecontacts.domain.model.contactdata.IContactDataIdExternal
import ch.abwesend.privatecontacts.domain.model.contactdata.PhoneNumber
import ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.factory.toLabel
import com.alexstyl.contactstore.ImageData
import com.alexstyl.contactstore.LabeledValue
import com.alexstyl.contactstore.MutableContact
import com.alexstyl.contactstore.Note
import com.alexstyl.contactstore.PhoneNumber as ContactStorePhoneNumber

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
        DELETED -> imageData = null
        CHANGED, NEW -> {
            // thumbnailUri seemingly cannot be changed
            imageData = newImage.fullImage?.let { ImageData(it) }
        }
    }
}

fun MutableContact.updateChangedContactData(changedContact: IContact) {
    updatePhoneNumbers(changedContact.contactDataSet)
    // TODO implement the others
}

private fun MutableContact.updatePhoneNumbers(contactData: List<ContactData>) {
    val phoneNumbers = contactData.filterIsInstance<PhoneNumber>()
    logger.debug("Updating ${phoneNumbers.size} phone numbers on contact $contactId")

    updateContactDataOfType(
        newContactData = phoneNumbers,
        mutableDataOnContact = phones,
    ) { newPhoneNumber -> ContactStorePhoneNumber(raw = newPhoneNumber.value) }
}

// TODO test
private fun <TInternal, TExternal: Any> MutableContact.updateContactDataOfType(
    newContactData: List<BaseGenericContactData<TInternal>>,
    mutableDataOnContact: MutableList<LabeledValue<TExternal>>,
    mapper: (BaseGenericContactData<TInternal>) -> TExternal,
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

private fun <T : Any> MutableList<LabeledValue<T>>.deleteContactData(
    contactData: ContactData,
    oldContactDataByNo: Map<Long?, LabeledValue<T>>,
) {
    val contactDataId = contactData.idExternal
    val correspondingOldData = oldContactDataByNo[contactDataId?.contactDataNo]
    correspondingOldData?.let { remove(it) }
}

private fun <TInternal, TExternal : Any> MutableList<LabeledValue<TExternal>>.upsertContactData(
    contactData: BaseGenericContactData<TInternal>,
    oldContactDataByNo: Map<Long?, LabeledValue<TExternal>>,
    mapper: (BaseGenericContactData<TInternal>) -> TExternal,
) {
    val contactDataId = contactData.idExternal
    val correspondingOldData = oldContactDataByNo[contactDataId?.contactDataNo]
    val newLabel = contactData.type.toLabel(contactData.category, correspondingOldData?.label)

    val transformedNewData = LabeledValue(
        value = mapper(contactData),
        label = newLabel
    )

    val indexOfOldNumber = indexOf(correspondingOldData)
    if (indexOfOldNumber >= 0) {
        this[indexOfOldNumber] = transformedNewData
    } else {
        add(transformedNewData)
    }
}

private val ContactData.idExternal: IContactDataIdExternal?
    get() = (id as? IContactDataIdExternal)
        .also { if (it == null) logger.warning("Phone number should have an external ID: $id") }
