/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.room.database

import android.content.Context
import ch.abwesend.privatecontacts.domain.util.getAnywhere
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import kotlin.reflect.KProperty

/**
 * Manages the access to the [database].
 * This allows the database to be replaced at runtime.
 */
class DatabaseHolder(context: Context) {
    private val databaseFactory: IDatabaseFactory<AppDatabase> by injectAnywhere()
    private val databaseInitializer: DatabaseInitializer by injectAnywhere()
    private val deletionHelper: DatabaseDeletionHelper by injectAnywhere()

    var database: AppDatabase
        private set

    init {
        database = databaseFactory.createDatabase(context)
    }

    operator fun getValue(thisRef: Any?, property: KProperty<*>): AppDatabase = database

    suspend fun resetDatabase(): Boolean {
        val context: Context = getAnywhere()

        database.close()
        val success = deletionHelper.resetDatabase(context)
        database = databaseFactory.createDatabase(context)
        ensureInitialized()

        return success
    }

    suspend fun ensureInitialized() {
        if (database.initialized || database.initializing.getAndSet(true)) {
            return
        }

        database.initialized = databaseInitializer.initializeDatabase(database.contactDao())
        database.initializing.set(false)
    }
}
