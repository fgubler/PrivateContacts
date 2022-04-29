/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository

import ch.abwesend.privatecontacts.domain.lib.coroutine.IDispatchers
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import ch.abwesend.privatecontacts.infrastructure.room.database.AppDatabase
import ch.abwesend.privatecontacts.infrastructure.room.database.DatabaseHolder
import kotlinx.coroutines.withContext

abstract class RepositoryBase {
    private val dispatchers: IDispatchers by injectAnywhere()
    protected val databaseHolder: DatabaseHolder by injectAnywhere()

    protected suspend fun <T> withDatabase(
        query: suspend (AppDatabase) -> T
    ): T = withContext(dispatchers.io) {
        databaseHolder.ensureInitialized()
        query(databaseHolder.database)
    }
}
