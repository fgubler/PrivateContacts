/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.contactdata

import ch.abwesend.privatecontacts.domain.model.ModelStatus
import ch.abwesend.privatecontacts.domain.model.ModelStatus.CHANGED

data class Company(
    override val id: ContactDataId,
    override val sortOrder: Int,
    override val type: ContactDataType,
    override val value: String,
    override val formattedValue: String = value,
    override val isMain: Boolean = false,
    override val modelStatus: ModelStatus,
) : StringBasedContactData<Company> {
    override val category: ContactDataCategory = ContactDataCategory.COMPANY

    override val allowedTypes: List<ContactDataType>
        get() = defaultAllowedTypes

    override fun changeValue(value: String): Company {
        val status = modelStatus.tryChangeTo(CHANGED)
        return copy(value = value, modelStatus = status)
    }

    override fun changeType(type: ContactDataType): Company {
        val status = modelStatus.tryChangeTo(CHANGED)
        return copy(type = type, modelStatus = status)
    }

    override fun delete(): Company {
        val status = modelStatus.tryChangeTo(ModelStatus.DELETED)
        return copy(modelStatus = status)
    }

    companion object {
        private val defaultAllowedTypes = listOf(
            ContactDataType.Main,
            ContactDataType.Other,
            ContactDataType.Custom,
        )
        fun createEmpty(sortOrder: Int): Company =
            Company(
                id = ContactDataId.randomId(),
                sortOrder = sortOrder,
                type = ContactDataType.Main,
                value = "",
                isMain = (sortOrder == 0),
                modelStatus = ModelStatus.NEW,
            )
    }
}
