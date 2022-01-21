package ch.abwesend.privatecontacts.domain.model.contactdata

import java.util.UUID

sealed interface ContactData {
    val id: UUID
    val sortOrder: Int? // ascending (0 comes first)
    val type: ContactDataSubType
    val isMain: Boolean
    val allowedTypes: List<ContactDataSubType>
    val isEmpty: Boolean
    val isNew: Boolean
}
