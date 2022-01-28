package ch.abwesend.privatecontacts.domain.model.contactdata

import ch.abwesend.privatecontacts.domain.model.ModelStatus
import ch.abwesend.privatecontacts.domain.model.ModelStatus.CHANGED

data class EmailAddress(
    override val id: ContactDataId,
    override val sortOrder: Int,
    override val type: ContactDataType,
    override val value: String,
    override val formattedValue: String = value,
    override val isMain: Boolean = false,
    override val modelStatus: ModelStatus,
) : StringBasedContactData<EmailAddress> {
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

    override fun delete(): EmailAddress {
        val status = modelStatus.tryChangeTo(ModelStatus.DELETED)
        return copy(modelStatus = status)
    }

    override fun formatValueForSearch(): String = value

    companion object {
        private val defaultAllowedTypes = listOf(
            ContactDataType.Private,
            ContactDataType.Business,
            ContactDataType.Other,
            ContactDataType.Custom,
        )
        fun createEmpty(sortOrder: Int): EmailAddress =
            EmailAddress(
                id = ContactDataId.randomId(),
                sortOrder = sortOrder,
                type = ContactDataType.Private,
                value = "",
                isMain = (sortOrder == 0),
                modelStatus = ModelStatus.NEW,
            )
    }
}
