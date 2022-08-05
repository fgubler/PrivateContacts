/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.contactdata

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.model.ModelStatus
import ch.abwesend.privatecontacts.domain.model.ModelStatus.CHANGED
import ch.abwesend.privatecontacts.domain.model.contact.ContactDataId
import ch.abwesend.privatecontacts.domain.model.contact.createContactDataId

data class PhysicalAddress(
    override val id: ContactDataId,
    override val sortOrder: Int,
    override val type: ContactDataType,
    override val value: String,
    override val isMain: Boolean = false,
    override val modelStatus: ModelStatus,
) : StringBasedContactDataGeneric<PhysicalAddress> {
    override val category: ContactDataCategory = ContactDataCategory.ADDRESS

    override val allowedTypes: List<ContactDataType>
        get() = defaultAllowedTypes

    override fun changeValue(value: String): PhysicalAddress {
        val status = modelStatus.tryChangeTo(CHANGED)
        return copy(value = value, modelStatus = status)
    }

    override fun changeType(type: ContactDataType): PhysicalAddress {
        val status = modelStatus.tryChangeTo(CHANGED)
        return copy(type = type, modelStatus = status)
    }

    override fun delete(): PhysicalAddress {
        val status = modelStatus.tryChangeTo(ModelStatus.DELETED)
        return copy(modelStatus = status)
    }

    companion object {
        val icon = Icons.Default.Home
        const val labelPlural = R.string.physical_addresses
        const val labelSingular = R.string.physical_address

        private val defaultAllowedTypes = listOf(
            ContactDataType.Personal,
            ContactDataType.Business,
            ContactDataType.Other,
            ContactDataType.Custom,
        )
        fun createEmpty(sortOrder: Int): PhysicalAddress =
            PhysicalAddress(
                id = createContactDataId(),
                sortOrder = sortOrder,
                type = ContactDataType.Personal,
                value = "",
                isMain = (sortOrder == 0),
                modelStatus = ModelStatus.NEW,
            )
    }
}
