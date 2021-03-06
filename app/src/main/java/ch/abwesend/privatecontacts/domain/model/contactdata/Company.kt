/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.contactdata

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.model.ModelStatus
import ch.abwesend.privatecontacts.domain.model.ModelStatus.CHANGED
import ch.abwesend.privatecontacts.domain.model.contact.ContactDataId
import ch.abwesend.privatecontacts.domain.model.contact.createContactDataId

data class Company(
    override val id: ContactDataId,
    override val sortOrder: Int,
    override val type: ContactDataType,
    override val value: String,
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
        val icon = Icons.Default.Apartment
        const val labelPlural = R.string.companies
        const val labelSingular = R.string.company

        private val defaultAllowedTypes = listOf(
            ContactDataType.Main,
            ContactDataType.Other,
            ContactDataType.Custom,
        )
        fun createEmpty(sortOrder: Int): Company =
            Company(
                id = createContactDataId(),
                sortOrder = sortOrder,
                type = ContactDataType.Main,
                value = "",
                isMain = (sortOrder == 0),
                modelStatus = ModelStatus.NEW,
            )
    }
}
