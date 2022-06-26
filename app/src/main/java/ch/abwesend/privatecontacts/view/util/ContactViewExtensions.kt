/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.util

import androidx.compose.ui.graphics.Color
import ch.abwesend.privatecontacts.domain.model.ModelStatus.DELETED
import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.model.contact.IContact
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactData
import ch.abwesend.privatecontacts.view.theme.AppColors

inline fun <reified T : ContactData> IContact.contactDataForDisplay(
    addEmptyElement: Boolean = true,
    noinline factory: (sortOrder: Int) -> T
): List<T> =
    contactDataSet
        .filterIsInstance<T>()
        .prepareForDisplay(addEmptyElement, factory)

fun <T : ContactData> List<T>.prepareForDisplay(
    addEmptyElement: Boolean = true,
    factory: (sortOrder: Int) -> T,
): List<T> {
    val sortedList = filter { it.modelStatus != DELETED }
        .sortedBy { it.sortOrder }

    val emptyElement: List<T> =
        if (any { it.isEmpty }) emptyList()
        else {
            val sortOrder = (maxOfOrNull { it.sortOrder } ?: -1) + 1
            listOf(factory(sortOrder))
        }

    return (sortedList + emptyElement).filter { addEmptyElement || !it.isEmpty }
}

fun <T : ContactData> MutableList<T>.addOrReplace(newData: T) {
    val indexOfExisting = indexOfFirst { it.id == newData.id }

    if (indexOfExisting >= 0) {
        set(indexOfExisting, newData)
    } else {
        add(newData)
    }
}

val ContactType.color: Color
    get() = when (this) {
        ContactType.SECRET -> AppColors.goodGreen
        ContactType.PUBLIC -> AppColors.dangerRed
    }
