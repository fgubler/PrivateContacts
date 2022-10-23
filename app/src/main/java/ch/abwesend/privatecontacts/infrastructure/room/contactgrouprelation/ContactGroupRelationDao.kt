/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.room.contactgrouprelation

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import java.util.UUID

@Dao
interface ContactGroupRelationDao {
    @Query("SELECT * FROM ContactGroupRelationEntity WHERE contactId = :contactId")
    suspend fun getRelationsForContact(contactId: UUID): List<ContactGroupRelationEntity>

    @Query("SELECT * FROM ContactGroupRelationEntity WHERE contactGroupName = :groupName")
    suspend fun getRelationsForContactGroup(groupName: String): List<ContactGroupRelationEntity>

    @Insert
    suspend fun insert(data: ContactGroupRelationEntity)

    @Insert
    suspend fun insertAll(data: List<ContactGroupRelationEntity>)

    @Delete
    suspend fun delete(data: ContactGroupRelationEntity)

    @Delete
    suspend fun deleteAll(data: List<ContactGroupRelationEntity>)

    @Query("DELETE FROM ContactGroupRelationEntity WHERE contactId = :contactId")
    suspend fun deleteRelationsForContact(contactId: UUID)
}
