package ch.abwesend.privatecontacts.view.util

import ch.abwesend.privatecontacts.domain.model.contact.Contact
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactData
import ch.abwesend.privatecontacts.domain.model.contactdata.PhoneNumber

val Contact.phoneNumbersForDisplay: List<PhoneNumber>
    get() = phoneNumbers.prepareForDisplay { PhoneNumber.createEmpty(it) }

fun <T : ContactData> List<T>.prepareForDisplay(
    factory: (sortOrder: Int) -> T
): List<T> {
    val sortedList = sortedBy { it.sortOrder ?: Int.MAX_VALUE }
    val emptyElement: List<T> =
        if (any { it.isEmpty }) emptyList()
        else listOf(factory(size))
    return sortedList + emptyElement
}
