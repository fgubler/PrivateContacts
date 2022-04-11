/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.room.contactdata

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import java.util.UUID

@Dao
interface ContactDataDao {
    @Query("SELECT * FROM ContactDataEntity WHERE contactId = :contactId")
    suspend fun getDataForContact(contactId: UUID): List<ContactDataEntity>

    @Query(
        """
        SELECT * 
        FROM ContactDataEntity 
        WHERE category = 'PHONE_NUMBER'
        AND valueRaw LIKE ('%' || :endOfPhoneNumber) 
        """
    )
    suspend fun findPhoneNumbersEndingOn(endOfPhoneNumber: String): List<ContactDataEntity>

    @Update
    suspend fun updateAll(data: List<ContactDataEntity>)

    @Insert
    suspend fun insert(data: ContactDataEntity)

    @Insert
    suspend fun insertAll(data: List<ContactDataEntity>)

    @Delete
    suspend fun delete(data: ContactDataEntity)

    @Delete
    suspend fun deleteAll(data: List<ContactDataEntity>)
}
