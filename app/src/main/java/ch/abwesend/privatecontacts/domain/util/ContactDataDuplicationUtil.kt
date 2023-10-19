package ch.abwesend.privatecontacts.domain.util

import ch.abwesend.privatecontacts.domain.model.contactdata.BaseGenericContactData
import ch.abwesend.privatecontacts.domain.model.contactdata.PhoneNumber

fun List<PhoneNumber>.removePhoneNumberDuplicates(): List<PhoneNumber> =
    removeDuplicates()
        .removeDuplicatesBy { it.formatValueForSearch() } // to remove duplicates with different formatting

fun <T : BaseGenericContactData<S>, S> List<T>.removeDuplicates(): List<T> =
    removeDuplicatesBy { it.displayValue }
        .removeDuplicatesBy { it.value }

private fun <T : BaseGenericContactData<S>, S, R> List<T>.removeDuplicatesBy(attributeSelector: (T) -> R): List<T> =
    groupBy(attributeSelector)
        .map { (_, value) -> value.minByOrNull { it.type.priority } }
        .filterNotNull()
