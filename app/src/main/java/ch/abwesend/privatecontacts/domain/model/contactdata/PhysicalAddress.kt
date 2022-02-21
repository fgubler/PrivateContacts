/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.contactdata

import ch.abwesend.privatecontacts.domain.model.ModelStatus
import ch.abwesend.privatecontacts.domain.model.ModelStatus.CHANGED

data class PhysicalAddress(
    override val id: ContactDataId,
    override val sortOrder: Int,
    override val type: ContactDataType,
    override val value: String,
    override val formattedValue: String = value,
    override val isMain: Boolean = false,
    override val modelStatus: ModelStatus,
) : StringBasedContactData<PhysicalAddress> {
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
        private val defaultAllowedTypes = listOf(
            ContactDataType.Private,
            ContactDataType.Business,
            ContactDataType.Other,
            ContactDataType.Custom,
        )
        fun createEmpty(sortOrder: Int): PhysicalAddress =
            PhysicalAddress(
                id = ContactDataId.randomId(),
                sortOrder = sortOrder,
                type = ContactDataType.Private,
                value = "",
                isMain = (sortOrder == 0),
                modelStatus = ModelStatus.NEW,
            )
    }
}
