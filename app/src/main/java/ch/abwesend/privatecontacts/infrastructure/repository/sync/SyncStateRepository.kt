/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository.sync

import ch.abwesend.privatecontacts.domain.model.contact.IContactIdInternal
import ch.abwesend.privatecontacts.domain.model.sync.ContactSyncState
import ch.abwesend.privatecontacts.domain.repository.ISyncStateRepository
import ch.abwesend.privatecontacts.infrastructure.repository.RepositoryBase
import ch.abwesend.privatecontacts.infrastructure.room.sync.toDomain
import ch.abwesend.privatecontacts.infrastructure.room.sync.toEntity

class SyncStateRepository : RepositoryBase(), ISyncStateRepository {
    override suspend fun getByContactId(contactId: IContactIdInternal): ContactSyncState? =
        withDatabase { database ->
            database.contactSyncStateDao().getByContactId(contactId.uuid)?.toDomain()
        }

    override suspend fun getBySyncGuid(syncGuid: String): ContactSyncState? =
        withDatabase { database ->
            database.contactSyncStateDao().getBySyncGuid(syncGuid)?.toDomain()
        }

    override suspend fun getAll(): List<ContactSyncState> =
        withDatabase { database ->
            database.contactSyncStateDao().getAll().map { it.toDomain() }
        }

    override suspend fun upsert(state: ContactSyncState) =
        withDatabase { database ->
            database.contactSyncStateDao().upsert(state.toEntity())
        }

    override suspend fun deleteByContactId(contactId: IContactIdInternal) =
        withDatabase { database ->
            database.contactSyncStateDao().deleteByContactId(contactId.uuid)
        }

    override suspend fun deleteAll() =
        withDatabase { database ->
            database.contactSyncStateDao().deleteAll()
        }
}
