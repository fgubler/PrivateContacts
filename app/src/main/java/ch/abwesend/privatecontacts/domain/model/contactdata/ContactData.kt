package ch.abwesend.privatecontacts.domain.model.contactdata

import java.util.UUID

interface ContactData {
    val id: UUID
    val sortOrder: Int?
    val type: ContactDataSubType
    val isMain: Boolean
}
