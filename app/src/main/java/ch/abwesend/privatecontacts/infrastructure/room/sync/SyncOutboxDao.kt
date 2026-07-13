/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.room.sync

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface SyncOutboxDao {
    @Query("SELECT * FROM SyncOutboxEntity ORDER BY sequenceNo ASC")
    suspend fun getAll(): List<SyncOutboxEntity>

    @Insert
    suspend fun insert(entity: SyncOutboxEntity): Long

    @Query("DELETE FROM SyncOutboxEntity WHERE sequenceNo in (:sequenceNumbers)")
    suspend fun delete(sequenceNumbers: List<Long>)

    @Query("SELECT COUNT(*) FROM SyncOutboxEntity")
    suspend fun count(): Int

    @Query("DELETE FROM SyncOutboxEntity")
    suspend fun deleteAll()
}
