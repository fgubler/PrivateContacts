package ch.abwesend.privatecontacts.view.util

import ch.abwesend.privatecontacts.domain.model.ModelStatus.DELETED
import ch.abwesend.privatecontacts.domain.model.contact.Contact
import ch.abwesend.privatecontacts.domain.model.contact.ContactFull
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactData
import ch.abwesend.privatecontacts.domain.model.contactdata.PhoneNumber

val Contact.phoneNumbersForDisplay: List<PhoneNumber>
    get() = phoneNumbers.prepareForDisplay { PhoneNumber.createEmpty(it) }

fun <T : ContactData> List<T>.prepareForDisplay(
    factory: (sortOrder: Int) -> T
): List<T> {
    val sortedList = filter { it.modelStatus != DELETED }
        .sortedBy { it.sortOrder }

    val emptyElement: List<T> =
        if (any { it.isEmpty }) emptyList()
        else {
            val sortOrder = (maxOfOrNull { it.sortOrder } ?: -1) + 1
            listOf(factory(sortOrder))
        }

    return sortedList + emptyElement
}

fun <T : ContactData> List<T>.addOrReplace(newData: T): List<T> =
    if (any { it.id == newData.id }) {
        map {
            if (it.id == newData.id) newData
            else it
        }
    } else {
        this + newData
    }

fun ContactFull.addOrReplaceContactDataEntry(newEntry: ContactData): ContactFull {
    val newContactDataSet = contactDataSet.addOrReplace(newEntry)
    return copy(contactDataSet = newContactDataSet)
}
