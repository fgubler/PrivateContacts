/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.room.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ch.abwesend.privatecontacts.infrastructure.room.contact.ContactDao
import ch.abwesend.privatecontacts.infrastructure.room.contact.ContactEntity
import ch.abwesend.privatecontacts.infrastructure.room.contactdata.ContactDataDao
import ch.abwesend.privatecontacts.infrastructure.room.contactdata.ContactDataEntity
import java.util.concurrent.atomic.AtomicBoolean

@Database(
    version = 18,
    exportSchema = true,
    entities = [ContactEntity::class, ContactDataEntity::class],
    autoMigrations = []
)
@TypeConverters(AppTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    var initialized: Boolean = false
    val initializing = AtomicBoolean(false)

    abstract fun contactDao(): ContactDao
    abstract fun contactDataDao(): ContactDataDao
}
