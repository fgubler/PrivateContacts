/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository

import ch.abwesend.privatecontacts.domain.lib.coroutine.IDispatchers
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import ch.abwesend.privatecontacts.infrastructure.room.database.AppDatabase
import kotlinx.coroutines.withContext

abstract class RepositoryBase {
    private val database: AppDatabase by injectAnywhere()
    private val dispatchers: IDispatchers by injectAnywhere()

    protected suspend fun <T> withDatabase(
        query: suspend (AppDatabase) -> T
    ): T = withDatabase(database, dispatchers, query)
}

suspend fun <T> withDatabase(
    database: AppDatabase,
    dispatchers: IDispatchers,
    query: suspend (AppDatabase) -> T
): T = withContext(dispatchers.io) {
    database.ensureInitialized()
    query(database)
}
