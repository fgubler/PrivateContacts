/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository.sync

import ch.abwesend.privatecontacts.domain.model.sync.SyncCursor
import ch.abwesend.privatecontacts.domain.repository.ISyncCursorRepository
import ch.abwesend.privatecontacts.infrastructure.repository.RepositoryBase
import ch.abwesend.privatecontacts.infrastructure.room.sync.toDomain
import ch.abwesend.privatecontacts.infrastructure.room.sync.toEntity

class SyncCursorRepository : RepositoryBase(), ISyncCursorRepository {
    override suspend fun getAll(): List<SyncCursor> =
        withDatabase { database ->
            database.syncCursorDao().getAll().map { it.toDomain() }
        }

    override suspend fun get(deviceId: String): SyncCursor? =
        withDatabase { database ->
            database.syncCursorDao().get(deviceId)?.toDomain()
        }

    override suspend fun upsert(cursor: SyncCursor) =
        withDatabase { database ->
            database.syncCursorDao().upsert(cursor.toEntity())
        }

    override suspend fun deleteAll() =
        withDatabase { database ->
            database.syncCursorDao().deleteAll()
        }
}
