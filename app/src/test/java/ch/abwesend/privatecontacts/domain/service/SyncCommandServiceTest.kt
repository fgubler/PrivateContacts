/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.service

import ch.abwesend.privatecontacts.domain.model.sync.ContactSyncState
import ch.abwesend.privatecontacts.domain.model.sync.SyncCommandOperation
import ch.abwesend.privatecontacts.domain.model.sync.SyncContact
import ch.abwesend.privatecontacts.domain.repository.ISyncOutboxRepository
import ch.abwesend.privatecontacts.domain.repository.ISyncStateRepository
import ch.abwesend.privatecontacts.domain.service.interfaces.ISyncContactMapper
import ch.abwesend.privatecontacts.domain.service.interfaces.ISyncScheduler
import ch.abwesend.privatecontacts.domain.settings.Settings
import ch.abwesend.privatecontacts.testutil.TestBase
import ch.abwesend.privatecontacts.testutil.databuilders.someInternalContactId
import ch.abwesend.privatecontacts.testutil.databuilders.someTestContact
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.SpyK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.runs
import io.mockk.unmockkObject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.core.module.Module

@ExperimentalCoroutinesApi
@ExtendWith(MockKExtension::class)
class SyncCommandServiceTest : TestBase() {
    @MockK
    private lateinit var syncStateRepository: ISyncStateRepository

    @MockK
    private lateinit var outboxRepository: ISyncOutboxRepository

    @MockK
    private lateinit var syncScheduler: ISyncScheduler

    @MockK
    private lateinit var contactMapper: ISyncContactMapper

    @SpyK
    private var underTest: SyncCommandService = SyncCommandService()

    override fun setup() {
        super.setup()
        mockkObject(Settings)
        coEvery { outboxRepository.enqueue(any()) } returns 1L
        coEvery { syncStateRepository.upsert(any()) } just runs
        every { syncScheduler.scheduleUploadDebounced() } just runs
        every { contactMapper.toSyncContact(any(), any()) } returns someSyncContact()
        every { contactMapper.serializeContact(any()) } returns "{}"
    }

    override fun tearDown() {
        unmockkObject(Settings)
        super.tearDown()
    }

    override fun setupKoinModule(module: Module) {
        super.setupKoinModule(module)
        module.single { syncStateRepository }
        module.single { outboxRepository }
        module.single { syncScheduler }
        module.single { contactMapper }
    }

    @Test
    fun `should enqueue a create command when sync is enabled`() {
        every { Settings.current } returns mockk { every { syncEnabled } returns true }
        val contactId = someInternalContactId()
        coEvery { syncStateRepository.getByContactId(contactId) } returns null

        runBlocking { underTest.enqueueUpsert(contactId, someTestContact(id = contactId), SyncCommandOperation.CREATE) }

        coVerify {
            outboxRepository.enqueue(
                match { it.operation == SyncCommandOperation.CREATE && it.baseRevision == null }
            )
        }
        coVerify { syncStateRepository.upsert(match { !it.deleted && it.dirty }) }
        coVerify { syncScheduler.scheduleUploadDebounced() }
    }

    @Test
    fun `should not enqueue anything when sync is disabled`() {
        every { Settings.current } returns mockk { every { syncEnabled } returns false }
        val contactId = someInternalContactId()

        runBlocking { underTest.enqueueUpsert(contactId, someTestContact(id = contactId), SyncCommandOperation.CREATE) }

        coVerify(exactly = 0) { outboxRepository.enqueue(any()) }
        coVerify(exactly = 0) { syncStateRepository.upsert(any()) }
    }

    @Test
    fun `should enqueue a delete and keep a tombstone`() {
        every { Settings.current } returns mockk { every { syncEnabled } returns true }
        val contactId = someInternalContactId()
        coEvery { syncStateRepository.getByContactId(contactId) } returns ContactSyncState(
            contactId = contactId,
            syncGuid = "guid",
            localRevision = "rev-1",
            lastSyncedRevision = "rev-1",
            dirty = false,
            deleted = false,
            deleteSyncedAtUtcMillis = null,
        )

        runBlocking { underTest.enqueueDeletes(listOf(contactId)) }

        coVerify { outboxRepository.enqueue(match { it.operation == SyncCommandOperation.DELETE }) }
        coVerify { syncStateRepository.upsert(match { it.deleted && it.dirty }) }
    }

    private fun someSyncContact(): SyncContact =
        SyncContact(
            syncId = "guid",
            firstName = "Jon",
            lastName = "Snow",
            nickname = "",
            middleName = "",
            namePrefix = "",
            nameSuffix = "",
            notes = "",
            contactData = emptyList(),
            groups = emptyList(),
            image = null,
        )
}
