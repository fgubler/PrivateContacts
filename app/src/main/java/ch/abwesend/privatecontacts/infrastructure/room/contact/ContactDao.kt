package ch.abwesend.privatecontacts.infrastructure.room.contact

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import java.util.UUID

@Dao
interface ContactDao {
    @Query("SELECT * FROM ContactEntity")
    fun getAll(): List<ContactEntity>

    @Query("SELECT COUNT(1) FROM ContactEntity")
    fun count(): Int

    @Query("SELECT * FROM ContactEntity WHERE id IN (:ids)")
    fun findByIds(ids: List<UUID>): List<ContactEntity>

    @Query(
        """
        SELECT * 
        FROM ContactEntity 
        WHERE (firstName || lastName || nickname) LIKE '%' || :query || '%' 
    """
    )
    fun searchByAnyName(query: String): List<ContactEntity>

    @Insert
    fun insert(contact: ContactEntity)

    @Insert
    fun insertAll(contacts: List<ContactEntity>)

    @Delete
    fun delete(contact: ContactEntity)
}
