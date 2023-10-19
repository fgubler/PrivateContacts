/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.contactdata

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.model.ModelStatus
import ch.abwesend.privatecontacts.domain.model.ModelStatus.CHANGED

data class Website(
    override val id: ContactDataId,
    override val sortOrder: Int,
    override val type: ContactDataType,
    override val value: String,
    override val isMain: Boolean = false,
    override val modelStatus: ModelStatus,
) : StringBasedContactDataGeneric<Website> {
    override val category: ContactDataCategory = ContactDataCategory.WEBSITE

    override val allowedTypes: List<ContactDataType>
        get() = defaultAllowedTypes

    override fun changeValue(value: String): Website {
        val status = modelStatus.tryChangeTo(CHANGED)
        return copy(value = value, modelStatus = status)
    }

    override fun changeType(type: ContactDataType): Website {
        val status = modelStatus.tryChangeTo(CHANGED)
        return copy(type = type, modelStatus = status)
    }

    override fun overrideStatus(newStatus: ModelStatus) = copy(modelStatus = newStatus)
    override fun changeToInternalId(): ContactData = copy(id = createContactDataId())
    override fun changeToExternalId(): ContactData = copy(id = createExternalDummyContactDataId())
    override fun changeSortOrder(newSortOrder: Int, updateStatus: Boolean): Website {
        val status = if (updateStatus) modelStatus.tryChangeTo(CHANGED) else modelStatus
        return copy(sortOrder = newSortOrder, modelStatus = status)
    }

    override fun delete(): Website {
        val status = modelStatus.tryChangeTo(ModelStatus.DELETED)
        return copy(modelStatus = status)
    }

    companion object {
        val icon = Icons.Default.Language
        const val labelPlural = R.string.websites
        const val labelSingular = R.string.website

        private val defaultAllowedTypes = listOf(
            ContactDataType.Personal,
            ContactDataType.Business,
            ContactDataType.Other,
            ContactDataType.Custom,
        )
        fun createEmpty(sortOrder: Int): Website =
            Website(
                id = createContactDataId(),
                sortOrder = sortOrder,
                type = ContactDataType.Personal,
                value = "",
                isMain = (sortOrder == 0),
                modelStatus = ModelStatus.NEW,
            )
    }
}
