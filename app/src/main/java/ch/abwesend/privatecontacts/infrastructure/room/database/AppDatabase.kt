package ch.abwesend.privatecontacts.infrastructure.room.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.infrastructure.room.contact.ContactDao
import ch.abwesend.privatecontacts.infrastructure.room.contact.ContactEntity
import ch.abwesend.privatecontacts.infrastructure.room.contact.toEntity
import ch.abwesend.privatecontacts.infrastructure.room.contactdata.ContactDataDao
import ch.abwesend.privatecontacts.infrastructure.room.contactdata.ContactDataEntity

@Database(
    version = 6,
    exportSchema = true,
    entities = [ContactEntity::class, ContactDataEntity::class],
    autoMigrations = []
)
@TypeConverters(AppTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    private var initialized: Boolean = false

    abstract fun contactDao(): ContactDao
    abstract fun contactDataDao(): ContactDataDao

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
