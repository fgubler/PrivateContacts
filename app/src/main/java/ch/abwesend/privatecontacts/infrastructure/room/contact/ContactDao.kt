package ch.abwesend.privatecontacts.infrastructure.room.contact

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import java.util.UUID

@Dao
interface ContactDao {
    @Query("SELECT * FROM ContactEntity")
    suspend fun getAll(): List<ContactEntity>

    @Query("SELECT * FROM ContactEntity ORDER BY firstName, lastName, id LIMIT :loadSize OFFSET :offsetInRows")
    suspend fun getPagedByFirstName(loadSize: Int, offsetInRows: Int): List<ContactEntity>

    @Query("SELECT * FROM ContactEntity ORDER BY lastName, firstName, id LIMIT :loadSize OFFSET :offsetInRows")
    suspend fun getPagedByLastName(loadSize: Int, offsetInRows: Int): List<ContactEntity>

    @Query(
        """
        SELECT * 
        FROM ContactEntity
        WHERE (
            fullTextSearch LIKE '%' || :query || '%' OR 
            (:phoneNumberQuery != '' AND fullTextSearch LIKE '%' || :phoneNumberQuery || '%')
        )
        ORDER BY firstName, lastName, id 
        LIMIT :loadSize 
        OFFSET :offsetInRows 
    """
    )
    suspend fun searchPagedByFirstName(
        query: String,
        phoneNumberQuery: String,
        loadSize: Int,
        offsetInRows: Int
    ): List<ContactEntity>

    @Query(
        """
        SELECT * 
        FROM ContactEntity
        WHERE (
            fullTextSearch LIKE '%' || :query || '%' OR 
            (:phoneNumberQuery != '' AND fullTextSearch LIKE '%' || :phoneNumberQuery || '%')
        )
        ORDER BY lastName, firstName, id 
        LIMIT :loadSize 
        OFFSET :offsetInRows 
    """
    )
    suspend fun searchPagedByLastName(
        query: String,
        phoneNumberQuery: String,
        loadSize: Int,
        offsetInRows: Int
    ): List<ContactEntity>

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

    @Update
    suspend fun update(contact: ContactEntity)

    @Insert
    suspend fun insert(contact: ContactEntity)

    @Insert
    suspend fun insertAll(contacts: List<ContactEntity>)

    @Delete
    suspend fun delete(contact: ContactEntity)
}
