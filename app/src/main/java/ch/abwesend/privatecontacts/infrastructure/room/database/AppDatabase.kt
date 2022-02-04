package ch.abwesend.privatecontacts.infrastructure.room.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.util.getAnywhere
import ch.abwesend.privatecontacts.infrastructure.room.contact.ContactDao
import ch.abwesend.privatecontacts.infrastructure.room.contact.ContactEntity
import ch.abwesend.privatecontacts.infrastructure.room.contactdata.ContactDataDao
import ch.abwesend.privatecontacts.infrastructure.room.contactdata.ContactDataEntity
import java.util.concurrent.atomic.AtomicBoolean

@Database(
    version = 14,
    exportSchema = true,
    entities = [ContactEntity::class, ContactDataEntity::class],
    autoMigrations = []
)
@TypeConverters(AppTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    private var initialized: Boolean = false
    private var initializing = AtomicBoolean(false)

    abstract fun contactDao(): ContactDao
    abstract fun contactDataDao(): ContactDataDao

    suspend fun ensureInitialized() {
        if (initialized || initializing.getAndSet(true)) return

        val hasData = contactDao().count() > 0

        if (!hasData) {
            logger.info("Initializing database")

            val initializer: IDatabaseFactory = getAnywhere()
            initializer.initializeDatabase()
            initialized = true
        }
    }
}
