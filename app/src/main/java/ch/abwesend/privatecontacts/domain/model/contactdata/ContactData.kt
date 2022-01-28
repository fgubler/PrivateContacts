package ch.abwesend.privatecontacts.domain.model.contactdata

import ch.abwesend.privatecontacts.domain.model.ModelStatus
import java.util.UUID

sealed interface ContactData {
    val id: UUID
    val sortOrder: Int? // ascending (0 comes first)
    val type: ContactDataType
    val isMain: Boolean
    val allowedTypes: List<ContactDataType>
    val isEmpty: Boolean
    val modelStatus: ModelStatus
}

interface StringBasedContactData : ContactData {
    val value: String
    val formattedValue: String
}
