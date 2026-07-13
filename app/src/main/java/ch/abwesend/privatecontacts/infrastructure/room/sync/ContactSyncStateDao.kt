/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.room.sync

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import java.util.UUID

@Dao
interface ContactSyncStateDao {
    @Query("SELECT * FROM ContactSyncStateEntity WHERE contactId = :contactId")
    suspend fun getByContactId(contactId: UUID): ContactSyncStateEntity?

    @Query("SELECT * FROM ContactSyncStateEntity WHERE syncGuid = :syncGuid")
    suspend fun getBySyncGuid(syncGuid: String): ContactSyncStateEntity?

    @Query("SELECT * FROM ContactSyncStateEntity")
    suspend fun getAll(): List<ContactSyncStateEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(state: ContactSyncStateEntity)

    @Query("DELETE FROM ContactSyncStateEntity WHERE contactId = :contactId")
    suspend fun deleteByContactId(contactId: UUID)

    @Query("DELETE FROM ContactSyncStateEntity")
    suspend fun deleteAll()
}
