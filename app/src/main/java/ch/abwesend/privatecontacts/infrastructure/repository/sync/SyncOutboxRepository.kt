/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository.sync

import ch.abwesend.privatecontacts.domain.model.sync.SyncOutboxCommand
import ch.abwesend.privatecontacts.domain.repository.ISyncOutboxRepository
import ch.abwesend.privatecontacts.infrastructure.repository.RepositoryBase
import ch.abwesend.privatecontacts.infrastructure.room.sync.toDomain
import ch.abwesend.privatecontacts.infrastructure.room.sync.toEntity

class SyncOutboxRepository : RepositoryBase(), ISyncOutboxRepository {
    override suspend fun enqueue(command: SyncOutboxCommand): Long =
        withDatabase { database ->
            database.syncOutboxDao().insert(command.toEntity())
        }

    override suspend fun getPending(): List<SyncOutboxCommand> =
        withDatabase { database ->
            database.syncOutboxDao().getAll().map { it.toDomain() }
        }

    override suspend fun delete(sequenceNumbers: List<Long>) =
        withDatabase { database ->
            database.syncOutboxDao().delete(sequenceNumbers)
        }

    override suspend fun count(): Int =
        withDatabase { database ->
            database.syncOutboxDao().count()
        }

    override suspend fun deleteAll() =
        withDatabase { database ->
            database.syncOutboxDao().deleteAll()
        }
}
