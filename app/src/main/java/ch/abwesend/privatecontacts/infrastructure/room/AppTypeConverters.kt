package ch.abwesend.privatecontacts.infrastructure.room

import androidx.room.TypeConverter
import ch.abwesend.privatecontacts.domain.model.ContactType
import java.util.UUID

class AppTypeConverters {
    // ContactType
    @TypeConverter
    fun serializeContactType(type: ContactType?): String? {
        return type?.name
    }
    @TypeConverter
    fun deserializeContactType(value: String?): ContactType? {
        return value?.let { ContactType.valueOf(it) }
    }

    // UUID
    @TypeConverter
    fun serializeUuid(uuid: UUID?): String? {
        return uuid?.toString()
    }
    @TypeConverter
    fun deserializeUuid(value: String?): UUID? {
        return value?.let { UUID.fromString(it) }
    }
}
