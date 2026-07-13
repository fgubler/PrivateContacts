/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository.sync

import ch.abwesend.privatecontacts.domain.model.sync.SyncConflict
import ch.abwesend.privatecontacts.domain.repository.ISyncConflictRepository
import ch.abwesend.privatecontacts.infrastructure.repository.RepositoryBase
import ch.abwesend.privatecontacts.infrastructure.room.sync.toDomain
import ch.abwesend.privatecontacts.infrastructure.room.sync.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class SyncConflictRepository : RepositoryBase(), ISyncConflictRepository {
    override fun getAllAsFlow(): Flow<List<SyncConflict>> =
        flow {
            databaseHolder.ensureInitialized()
            val conflicts = databaseHolder.database.syncConflictDao().getAllAsFlow()
                .map { entities -> entities.map { it.toDomain() } }
            emitAll(conflicts)
        }

    override suspend fun getAll(): List<SyncConflict> =
        withDatabase { database ->
            database.syncConflictDao().getAll().map { it.toDomain() }
        }

    override suspend fun upsert(conflict: SyncConflict) =
        withDatabase { database ->
            database.syncConflictDao().upsert(conflict.toEntity())
        }

    override suspend fun resolve(syncGuid: String) =
        withDatabase { database ->
            database.syncConflictDao().resolve(syncGuid)
        }

    override suspend fun deleteAll() =
        withDatabase { database ->
            database.syncConflictDao().deleteAll()
        }
}
