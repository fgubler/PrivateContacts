/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.room.contactimage

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import java.util.UUID

@Dao
interface ContactImageDao {
    @Query("SELECT * FROM ContactImageEntity WHERE contactId = :contactId")
    suspend fun getImage(contactId: UUID): ContactImageEntity?

    @Insert
    suspend fun insert(data: ContactImageEntity)

    @Insert
    suspend fun insertAll(data: List<ContactImageEntity>)

    @Delete
    suspend fun delete(data: ContactImageEntity)

    @Query("DELETE FROM ContactImageEntity WHERE contactId = :contactId")
    suspend fun deleteImage(contactId: UUID)

    @Delete
    suspend fun deleteAll(data: List<ContactImageEntity>)
}
