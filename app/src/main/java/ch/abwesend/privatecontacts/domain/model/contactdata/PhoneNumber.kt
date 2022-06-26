/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.contactdata

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.model.ModelStatus
import ch.abwesend.privatecontacts.domain.model.ModelStatus.CHANGED
import ch.abwesend.privatecontacts.domain.model.contact.ContactDataId
import ch.abwesend.privatecontacts.domain.model.contact.createContactDataId

data class PhoneNumber(
    override val id: ContactDataId,
    override val sortOrder: Int,
    override val type: ContactDataType,
    override val value: String,
    override val formattedValue: String = value,
    override val valueForMatching: String = value,
    override val isMain: Boolean = false,
    override val modelStatus: ModelStatus,
) : StringBasedContactData<PhoneNumber> {
    override val category: ContactDataCategory = ContactDataCategory.PHONE_NUMBER

    override val allowedTypes: List<ContactDataType>
        get() = defaultAllowedTypes

    override fun changeValue(value: String): PhoneNumber {
        val status = modelStatus.tryChangeTo(CHANGED)
        return copy(value = value, modelStatus = status)
    }

    override fun changeType(type: ContactDataType): PhoneNumber {
        val status = modelStatus.tryChangeTo(CHANGED)
        return copy(type = type, modelStatus = status)
    }

    override fun delete(): PhoneNumber {
        val status = modelStatus.tryChangeTo(ModelStatus.DELETED)
        return copy(modelStatus = status)
    }

    override fun formatValueForSearch(): String = formatValueForSearch(value)

    companion object {
        val icon = Icons.Default.Phone
        const val labelSingular = R.string.phone_number
        const val labelPlural = R.string.phone_numbers

        private val defaultAllowedTypes = listOf(
            ContactDataType.Mobile,
            ContactDataType.Personal,
            ContactDataType.Business,
            ContactDataType.Other,
            ContactDataType.Custom,
        )
        fun createEmpty(sortOrder: Int): PhoneNumber =
            PhoneNumber(
                id = createContactDataId(),
                sortOrder = sortOrder,
                type = ContactDataType.Mobile,
                value = "",
                isMain = (sortOrder == 0),
                modelStatus = ModelStatus.NEW,
            )

        fun formatValueForSearch(value: String): String =
            value.filter { it.isDigit() }
    }
}

@JvmInline
value class PhoneNumberValue(val value: String)
