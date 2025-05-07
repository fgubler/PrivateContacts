/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.room.contactgroup

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactGroupDao {
    @Query("SELECT * FROM ContactGroupEntity ORDER BY name ASC")
    fun getAllAsFlow(): Flow<List<ContactGroupEntity>>

    @Query("SELECT * FROM ContactGroupEntity WHERE name = :groupName")
    suspend fun getGroup(groupName: String): ContactGroupEntity

    @Query("SELECT * FROM ContactGroupEntity WHERE name in (:groupNames)")
    suspend fun getGroups(groupNames: List<String>): List<ContactGroupEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE) // must not replace existing groups because that would delete their relations
    suspend fun upsertAll(data: List<ContactGroupEntity>)

    @Delete
    suspend fun delete(data: ContactGroupEntity)

    @Delete
    suspend fun deleteAll(data: List<ContactGroupEntity>)
}
