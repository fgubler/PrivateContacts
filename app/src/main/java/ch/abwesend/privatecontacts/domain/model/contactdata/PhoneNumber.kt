package ch.abwesend.privatecontacts.domain.model.contactdata

import ch.abwesend.privatecontacts.domain.model.ModelStatus
import java.util.UUID

data class PhoneNumber(
    override val id: UUID,
    override val sortOrder: Int?,
    override val type: ContactDataSubType,
    override val value: String,
    override val formattedValue: String = value,
    override val isMain: Boolean = false,
    override val modelStatus: ModelStatus,
) : StringBasedContactData {
    override val allowedTypes: List<ContactDataSubType> = defaultAllowedTypes

    override val isEmpty: Boolean = value.isEmpty()

    companion object {
        private val defaultAllowedTypes = listOf(
            ContactDataSubType.Mobile,
            ContactDataSubType.Private,
            ContactDataSubType.Business,
            ContactDataSubType.Other,
            ContactDataSubType.Custom,
        )
        fun createEmpty(sortOrder: Int): PhoneNumber =
            PhoneNumber(
                id = UUID.randomUUID(),
                sortOrder = sortOrder,
                type = ContactDataSubType.Mobile,
                value = "",
                isMain = (sortOrder == 0),
                modelStatus = ModelStatus.NEW,
            )
    }
}
