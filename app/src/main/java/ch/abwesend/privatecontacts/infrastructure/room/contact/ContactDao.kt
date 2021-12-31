package ch.abwesend.privatecontacts.infrastructure.room.contact

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import java.util.UUID

@Dao
interface ContactDao {
    @Query("SELECT * FROM ContactEntity")
    suspend fun getAll(): List<ContactEntity>

    @Query("SELECT COUNT(1) FROM ContactEntity")
    suspend fun count(): Int

    @Query("SELECT * FROM ContactEntity WHERE id IN (:ids)")
    suspend fun findByIds(ids: List<UUID>): List<ContactEntity>

    @Query(
        """
        SELECT * 
        FROM ContactEntity 
        WHERE (firstName || lastName || nickname) LIKE '%' || :query || '%' 
    """
    )
    suspend fun searchByAnyName(query: String): List<ContactEntity>

    @Insert
    suspend fun insert(contact: ContactEntity)

    @Insert
    suspend fun insertAll(contacts: List<ContactEntity>)

    @Delete
    suspend fun delete(contact: ContactEntity)
}
