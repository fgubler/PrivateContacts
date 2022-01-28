package ch.abwesend.privatecontacts.domain.model.contactdata

import ch.abwesend.privatecontacts.domain.model.ModelStatus
import ch.abwesend.privatecontacts.domain.model.ModelStatus.CHANGED

data class PhoneNumber(
    override val id: ContactDataId,
    override val sortOrder: Int,
    override val type: ContactDataType,
    override val value: String,
    override val formattedValue: String = value,
    override val isMain: Boolean = false,
    override val modelStatus: ModelStatus,
) : StringBasedContactData<PhoneNumber> {
    override val allowedTypes: List<ContactDataType>
        get() = defaultAllowedTypes

    override val isEmpty: Boolean = value.isEmpty()

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

    override fun formatValueForSearch(): String = value.filter { it.isDigit() }

    companion object {
        private val defaultAllowedTypes = listOf(
            ContactDataType.Mobile,
            ContactDataType.Private,
            ContactDataType.Business,
            ContactDataType.Other,
            ContactDataType.Custom,
        )
        fun createEmpty(sortOrder: Int): PhoneNumber =
            PhoneNumber(
                id = ContactDataId.randomId(),
                sortOrder = sortOrder,
                type = ContactDataType.Mobile,
                value = "",
                isMain = (sortOrder == 0),
                modelStatus = ModelStatus.NEW,
            )
    }
}
