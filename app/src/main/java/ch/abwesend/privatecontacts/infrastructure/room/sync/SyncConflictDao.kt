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
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncConflictDao {
    @Query("SELECT * FROM SyncConflictEntity ORDER BY detectedAtUtcMillis DESC")
    fun getAllAsFlow(): Flow<List<SyncConflictEntity>>

    @Query("SELECT * FROM SyncConflictEntity ORDER BY detectedAtUtcMillis DESC")
    suspend fun getAll(): List<SyncConflictEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(conflict: SyncConflictEntity)

    @Query("DELETE FROM SyncConflictEntity WHERE syncGuid = :syncGuid")
    suspend fun resolve(syncGuid: String)

    @Query("DELETE FROM SyncConflictEntity")
    suspend fun deleteAll()
}
