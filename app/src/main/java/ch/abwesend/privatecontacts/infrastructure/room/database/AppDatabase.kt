/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.room.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ch.abwesend.privatecontacts.infrastructure.room.contact.ContactDao
import ch.abwesend.privatecontacts.infrastructure.room.contact.ContactEntity
import ch.abwesend.privatecontacts.infrastructure.room.contactdata.ContactDataDao
import ch.abwesend.privatecontacts.infrastructure.room.contactdata.ContactDataEntity
import ch.abwesend.privatecontacts.infrastructure.room.contactgroup.ContactGroupDao
import ch.abwesend.privatecontacts.infrastructure.room.contactgroup.ContactGroupEntity
import ch.abwesend.privatecontacts.infrastructure.room.contactgrouprelation.ContactGroupRelationDao
import ch.abwesend.privatecontacts.infrastructure.room.contactgrouprelation.ContactGroupRelationEntity
import java.util.concurrent.atomic.AtomicBoolean

@Database(
    version = 22,
    exportSchema = true,
    entities = [
        ContactEntity::class,
        ContactDataEntity::class,
        ContactGroupEntity::class,
        ContactGroupRelationEntity::class,
    ],
    autoMigrations = [
        AutoMigration(from = 18, to = 19),
        AutoMigration(from = 19, to = 20),
        AutoMigration(from = 20, to = 21),
        AutoMigration(from = 21, to = 22),
    ]
)
@TypeConverters(AppTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    var initialized: Boolean = false
    val initializing = AtomicBoolean(false)

    abstract fun contactDao(): ContactDao
    abstract fun contactDataDao(): ContactDataDao
    abstract fun contactGroupDao(): ContactGroupDao
    abstract fun contactGroupRelationDao(): ContactGroupRelationDao
}
