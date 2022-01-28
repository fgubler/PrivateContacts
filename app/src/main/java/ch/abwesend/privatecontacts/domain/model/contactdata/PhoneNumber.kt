package ch.abwesend.privatecontacts.domain.model.contactdata

import ch.abwesend.privatecontacts.domain.model.ModelStatus
import java.util.UUID

data class PhoneNumber(
    override val id: UUID,
    override val sortOrder: Int?,
    override val type: ContactDataType,
    override val value: String,
    override val formattedValue: String = value,
    override val isMain: Boolean = false,
    override val modelStatus: ModelStatus,
) : StringBasedContactData {
    override val allowedTypes: List<ContactDataType> = defaultAllowedTypes

    override val isEmpty: Boolean = value.isEmpty()

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
                id = UUID.randomUUID(),
                sortOrder = sortOrder,
                type = ContactDataType.Mobile,
                value = "",
                isMain = (sortOrder == 0),
                modelStatus = ModelStatus.NEW,
            )
    }
}
