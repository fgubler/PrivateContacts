package ch.abwesend.privatecontacts.infrastructure.room.database

import androidx.room.TypeConverter
import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataSubType
import ch.abwesend.privatecontacts.infrastructure.room.contactdata.ContactDataType
import java.util.UUID

object AppTypeConverters {
    // UUID
    @TypeConverter
    fun serializeUuid(uuid: UUID?): String? {
        return uuid?.toString()
    }
    @TypeConverter
    fun deserializeUuid(value: String?): UUID? {
        return value?.let { UUID.fromString(it) }
    }

    // ContactType
    @TypeConverter
    fun serializeContactType(type: ContactType?): String? {
        return type?.name
    }
    @TypeConverter
    fun deserializeContactType(value: String?): ContactType? {
        return value?.let { ContactType.valueOf(it) }
    }

    // ContactDataType
    @TypeConverter
    fun serializeContactDataType(type: ContactDataType?): String? {
        return type?.name
    }
    @TypeConverter
    fun deserializeContactDataType(value: String?): ContactDataType? {
        return value?.let { ContactDataType.valueOf(it) }
    }

    // ContactDataSubType.Key
    @TypeConverter
    fun serializeContactDataSubType(type: ContactDataSubType.Key?): String? {
        return type?.name
    }
    @TypeConverter
    fun deserializeContactDataSubType(value: String?): ContactDataSubType.Key? {
        return value?.let { ContactDataSubType.Key.valueOf(it) }
    }
}
