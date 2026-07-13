/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.repository

import ch.abwesend.privatecontacts.domain.model.contact.IContactIdInternal
import ch.abwesend.privatecontacts.domain.model.sync.ContactSyncState
import ch.abwesend.privatecontacts.domain.model.sync.SyncConflict
import ch.abwesend.privatecontacts.domain.model.sync.SyncCursor
import ch.abwesend.privatecontacts.domain.model.sync.SyncOutboxCommand
import kotlinx.coroutines.flow.Flow

interface ISyncStateRepository {
    suspend fun getByContactId(contactId: IContactIdInternal): ContactSyncState?
    suspend fun getBySyncGuid(syncGuid: String): ContactSyncState?
    suspend fun getAll(): List<ContactSyncState>
    suspend fun upsert(state: ContactSyncState)
    suspend fun deleteByContactId(contactId: IContactIdInternal)
    suspend fun deleteAll()
}

interface ISyncOutboxRepository {
    /** @return the assigned auto-generated sequence number */
    suspend fun enqueue(command: SyncOutboxCommand): Long
    suspend fun getPending(): List<SyncOutboxCommand>
    suspend fun delete(sequenceNumbers: List<Long>)
    suspend fun count(): Int
    suspend fun deleteAll()
}

interface ISyncCursorRepository {
    suspend fun getAll(): List<SyncCursor>
    suspend fun get(deviceId: String): SyncCursor?
    suspend fun upsert(cursor: SyncCursor)
    suspend fun deleteAll()
}

interface ISyncConflictRepository {
    fun getAllAsFlow(): Flow<List<SyncConflict>>
    suspend fun getAll(): List<SyncConflict>
    suspend fun upsert(conflict: SyncConflict)
    suspend fun resolve(syncGuid: String)
    suspend fun deleteAll()
}
