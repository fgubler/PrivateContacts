/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.contactdata

import ch.abwesend.privatecontacts.domain.model.ModelStatus
import ch.abwesend.privatecontacts.domain.model.WithModelStatus

sealed interface ContactData : WithModelStatus {
    val id: ContactDataId
    val sortOrder: Int // ascending (0 comes first)
    val category: ContactDataCategory
    val type: ContactDataType
    val isMain: Boolean
    val allowedTypes: List<ContactDataType>
    val isEmpty: Boolean

    val displayValue: String
    val value: Any?

    fun changeType(type: ContactDataType): ContactData
    fun overrideStatus(newStatus: ModelStatus): ContactData
    fun changeToInternalId(): ContactData
    fun changeToExternalId(): ContactData
    fun changeSortOrder(newSortOrder: Int): ContactData
    fun delete(): ContactData

    /** returning null means that the data will be ignored in search */
    fun formatValueForSearch(): String?
}
