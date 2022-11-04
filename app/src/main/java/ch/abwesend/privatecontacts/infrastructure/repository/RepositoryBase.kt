/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository

import ch.abwesend.privatecontacts.domain.lib.coroutine.IDispatchers
import ch.abwesend.privatecontacts.domain.lib.coroutine.mapAsync
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import ch.abwesend.privatecontacts.infrastructure.room.database.AppDatabase
import ch.abwesend.privatecontacts.infrastructure.room.database.DatabaseHolder
import kotlinx.coroutines.withContext

internal const val MAX_BULK_OPERATION_SIZE = 500 // rather arbitrary, SQLite should be able to handle 999

abstract class RepositoryBase {
    private val dispatchers: IDispatchers by injectAnywhere()
    protected val databaseHolder: DatabaseHolder by injectAnywhere()

    protected suspend fun <T> withDatabase(
        query: suspend (AppDatabase) -> T
    ): T = withContext(dispatchers.io) {
        databaseHolder.ensureInitialized()
        query(databaseHolder.database)
    }

    protected suspend fun <T, S> bulkOperation(
        data: Collection<T>,
        operation: suspend (AppDatabase, Collection<T>) -> S
    ): List<S> = withDatabase { database ->
        data.chunked(MAX_BULK_OPERATION_SIZE).mapAsync { operation(database, data) }
    }

    protected suspend fun <T, S> bulkLoadingOperation(
        data: Collection<T>,
        operation: suspend (AppDatabase, Collection<T>) -> Collection<S>
    ): List<S> = bulkOperation(data, operation).flatten()
}
