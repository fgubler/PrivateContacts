package ch.abwesend.privatecontacts.infrastructure.room.contactdata

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ContactDataDao {
    @Query("SELECT * FROM ContactDataEntity WHERE contactId = :contactId")
    fun getDataForContact(contactId: Int): List<ContactDataEntity>

    @Insert
    fun insert(data: ContactDataEntity)

    @Insert
    fun insertAll(data: List<ContactDataEntity>)

    @Delete
    fun delete(data: ContactDataEntity)
}
