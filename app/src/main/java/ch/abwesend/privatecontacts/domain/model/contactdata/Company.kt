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

data class Company(
    override val id: ContactDataId,
    override val sortOrder: Int,
    override val type: ContactDataType,
    override val value: String,
    override val isMain: Boolean = false,
    override val modelStatus: ModelStatus,
) : StringBasedContactDataGeneric<Company> {
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

    override fun overrideStatus(newStatus: ModelStatus) = copy(modelStatus = newStatus)
    override fun changeToInternalId(): ContactData = copy(id = createContactDataId())
    override fun changeToExternalId(): ContactData = copy(id = createExternalDummyContactDataId())

    override fun delete(): Company {
        val status = modelStatus.tryChangeTo(ModelStatus.DELETED)
        return copy(modelStatus = status)
    }

    companion object {
        val icon = Icons.Default.Apartment
        val labelPlural get() = R.string.companies
        val labelSingular get() = R.string.company

        private val defaultAllowedTypes = listOf(
            ContactDataType.Main,
            ContactDataType.Other,
            ContactDataType.Custom,
        )
        fun createEmpty(sortOrder: Int): Company {
            val isMain = (sortOrder == 0)
            return Company(
                id = createContactDataId(),
                sortOrder = sortOrder,
                type = if (isMain) ContactDataType.Main else ContactDataType.Other,
                value = "",
                isMain = isMain,
                modelStatus = ModelStatus.NEW,
            )
        }
    }
}
