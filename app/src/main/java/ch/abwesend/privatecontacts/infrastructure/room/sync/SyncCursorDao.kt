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

@Dao
interface SyncCursorDao {
    @Query("SELECT * FROM SyncCursorEntity")
    suspend fun getAll(): List<SyncCursorEntity>

    @Query("SELECT * FROM SyncCursorEntity WHERE deviceId = :deviceId")
    suspend fun get(deviceId: String): SyncCursorEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(cursor: SyncCursorEntity)

    @Query("DELETE FROM SyncCursorEntity")
    suspend fun deleteAll()
}
