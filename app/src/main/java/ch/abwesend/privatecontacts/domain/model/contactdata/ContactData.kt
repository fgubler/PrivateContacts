package ch.abwesend.privatecontacts.domain.model.contactdata

import ch.abwesend.privatecontacts.domain.model.ModelStatus
import java.util.UUID

sealed interface ContactData {
    val id: ContactDataId
    val sortOrder: Int // ascending (0 comes first)
    val type: ContactDataType
    val isMain: Boolean
    val allowedTypes: List<ContactDataType>
    val isEmpty: Boolean
    val modelStatus: ModelStatus

    fun changeType(type: ContactDataType): ContactData
    fun delete(): ContactData

    /** returning null means that the data will be ignored in search */
    fun formatValueForSearch(): String?
}

@JvmInline
value class ContactDataId(val uuid: UUID) {
    companion object {
        fun randomId(): ContactDataId = ContactDataId(UUID.randomUUID())
    }
}

interface StringBasedContactDataSimple : ContactData {
    val value: String
    val formattedValue: String
}

/**
 * The generics are needed to make the functions return the dynamic type of "this"
 */
interface StringBasedContactData<T : StringBasedContactData<T>> : StringBasedContactDataSimple {
    fun changeValue(value: String): T
    override fun changeType(type: ContactDataType): T
    override fun delete(): T
}
