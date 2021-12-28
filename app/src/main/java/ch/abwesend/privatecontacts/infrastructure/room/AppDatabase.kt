package ch.abwesend.privatecontacts.infrastructure.room

import androidx.room.Database
import androidx.room.RoomDatabase
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.infrastructure.room.contact.ContactDao
import ch.abwesend.privatecontacts.infrastructure.room.contact.ContactEntity
import ch.abwesend.privatecontacts.infrastructure.room.contact.toEntity

@Database(
    version = 2,
    exportSchema = true,
    entities = [ContactEntity::class],
)
abstract class AppDatabase : RoomDatabase() {
    private var initialized: Boolean = false

    abstract fun contactDao(): ContactDao

    fun ensureInitialized() {
        if (initialized) return

        val hasData = contactDao().count() > 0

        if (!hasData) {
            logger.info("Initializing database")

            // TODO remove?
            val entities = dummyContacts.map { it.toEntity() }
            contactDao().insertAll(entities)
            initialized = true
        }
    }
}
