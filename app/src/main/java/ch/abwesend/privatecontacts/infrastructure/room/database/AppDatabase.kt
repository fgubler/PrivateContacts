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
import ch.abwesend.privatecontacts.infrastructure.room.contactimage.ContactImageDao
import ch.abwesend.privatecontacts.infrastructure.room.contactimage.ContactImageEntity
import ch.abwesend.privatecontacts.infrastructure.room.sync.ContactSyncStateDao
import ch.abwesend.privatecontacts.infrastructure.room.sync.ContactSyncStateEntity
import ch.abwesend.privatecontacts.infrastructure.room.sync.SyncConflictDao
import ch.abwesend.privatecontacts.infrastructure.room.sync.SyncConflictEntity
import ch.abwesend.privatecontacts.infrastructure.room.sync.SyncCursorDao
import ch.abwesend.privatecontacts.infrastructure.room.sync.SyncCursorEntity
import ch.abwesend.privatecontacts.infrastructure.room.sync.SyncOutboxDao
import ch.abwesend.privatecontacts.infrastructure.room.sync.SyncOutboxEntity
import java.util.concurrent.atomic.AtomicBoolean

@Database(
    version = 29,
    exportSchema = true,
    entities = [
        ContactEntity::class,
        ContactDataEntity::class,
        ContactGroupEntity::class,
        ContactGroupRelationEntity::class,
        ContactImageEntity::class,
        ContactSyncStateEntity::class,
        SyncOutboxEntity::class,
        SyncCursorEntity::class,
        SyncConflictEntity::class,
    ],
    autoMigrations = [
        AutoMigration(from = 18, to = 19),
        AutoMigration(from = 19, to = 20),
        AutoMigration(from = 20, to = 21),
        AutoMigration(from = 21, to = 22),
        AutoMigration(from = 22, to = 23),
        AutoMigration(from = 23, to = 24),
        AutoMigration(from = 24, to = 25),
        AutoMigration(from = 25, to = 26),
        AutoMigration(from = 26, to = 27),
        AutoMigration(from = 27, to = 28),
        AutoMigration(from = 28, to = 29),
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
    abstract fun contactImageDao(): ContactImageDao
    abstract fun contactSyncStateDao(): ContactSyncStateDao
    abstract fun syncOutboxDao(): SyncOutboxDao
    abstract fun syncCursorDao(): SyncCursorDao
    abstract fun syncConflictDao(): SyncConflictDao
}
