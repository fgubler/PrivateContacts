package ch.abwesend.privatecontacts.domain.model.contactdata

import java.util.UUID

data class PhoneNumber(
    override val id: UUID,
    override val sortOrder: Int?,
    override val type: ContactDataSubType,
    override val isMain: Boolean,
    val value: String,
) : ContactData
