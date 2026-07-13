/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.room.database

import androidx.room.TypeConverter
import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataCategory
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType
import ch.abwesend.privatecontacts.domain.model.sync.ConflictKind
import ch.abwesend.privatecontacts.domain.model.sync.SyncCommandOperation
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

    // ContactDataCategory
    @TypeConverter
    fun serializeContactDataCategory(category: ContactDataCategory?): String? {
        return category?.name
    }

    @TypeConverter
    fun deserializeContactDataCategory(value: String?): ContactDataCategory? {
        return value?.let { ContactDataCategory.valueOf(it) }
    }

    // ContactDataType.Key
    @TypeConverter
    fun serializeContactDataType(type: ContactDataType.Key?): String? {
        return type?.name
    }

    @TypeConverter
    fun deserializeContactDataType(value: String?): ContactDataType.Key? {
        return value?.let { ContactDataType.Key.valueOf(it) }
    }

    // SyncCommandOperation
    @TypeConverter
    fun serializeSyncCommandOperation(operation: SyncCommandOperation?): String? {
        return operation?.name
    }

    @TypeConverter
    fun deserializeSyncCommandOperation(value: String?): SyncCommandOperation? {
        return value?.let { SyncCommandOperation.valueOf(it) }
    }

    // ConflictKind
    @TypeConverter
    fun serializeConflictKind(kind: ConflictKind?): String? {
        return kind?.name
    }

    @TypeConverter
    fun deserializeConflictKind(value: String?): ConflictKind? {
        return value?.let { ConflictKind.valueOf(it) }
    }
}
