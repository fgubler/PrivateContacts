package ch.abwesend.privatecontacts.domain.model.contactdata

import java.util.UUID

data class PhoneNumber(
    override val id: UUID,
    override val sortOrder: Int?,
    override val type: ContactDataSubType,
    override val isMain: Boolean,
    override val isNew: Boolean,
    val value: String,
) : ContactData {
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
                isMain = (sortOrder == 0),
                isNew = true,
                value = ""
            )
    }
}
