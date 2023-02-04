/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.contactdata

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.model.ModelStatus
import ch.abwesend.privatecontacts.domain.model.ModelStatus.CHANGED

data class EmailAddress(
    override val id: ContactDataId,
    override val sortOrder: Int,
    override val type: ContactDataType,
    override val value: String,
    override val isMain: Boolean = false,
    override val modelStatus: ModelStatus,
) : StringBasedContactDataGeneric<EmailAddress> {
    override val category: ContactDataCategory = ContactDataCategory.EMAIL

    override val allowedTypes: List<ContactDataType>
        get() = defaultAllowedTypes

    override fun changeValue(value: String): EmailAddress {
        val status = modelStatus.tryChangeTo(CHANGED)
        return copy(value = value, modelStatus = status)
    }

    override fun changeType(type: ContactDataType): EmailAddress {
        val status = modelStatus.tryChangeTo(CHANGED)
        return copy(type = type, modelStatus = status)
    }

    override fun overrideStatus(newStatus: ModelStatus) = copy(modelStatus = newStatus)
    override fun changeToInternalId(): ContactData = copy(id = createContactDataId())
    override fun changeToExternalId(): ContactData = copy(id = createExternalDummyContactDataId())

    override fun delete(): EmailAddress {
        val status = modelStatus.tryChangeTo(ModelStatus.DELETED)
        return copy(modelStatus = status)
    }

    companion object {
        val icon = Icons.Default.Email
        const val labelPlural = R.string.email_addresses
        const val labelSingular = R.string.email_address

        private val defaultAllowedTypes = listOf(
            ContactDataType.Personal,
            ContactDataType.Business,
            ContactDataType.Other,
            ContactDataType.Custom,
        )
        fun createEmpty(sortOrder: Int): EmailAddress =
            EmailAddress(
                id = createContactDataId(),
                sortOrder = sortOrder,
                type = ContactDataType.Personal,
                value = "",
                isMain = (sortOrder == 0),
                modelStatus = ModelStatus.NEW,
            )
    }
}
