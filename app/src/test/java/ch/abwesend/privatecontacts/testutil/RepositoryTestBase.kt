/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.testutil

import ch.abwesend.privatecontacts.infrastructure.room.contact.ContactDao
import ch.abwesend.privatecontacts.infrastructure.room.contactdata.ContactDataDao
import ch.abwesend.privatecontacts.infrastructure.room.contactgroup.ContactGroupDao
import ch.abwesend.privatecontacts.infrastructure.room.contactgrouprelation.ContactGroupRelationDao
import ch.abwesend.privatecontacts.infrastructure.room.contactimage.ContactImageDao
import ch.abwesend.privatecontacts.infrastructure.room.database.AppDatabase
import ch.abwesend.privatecontacts.infrastructure.room.database.DatabaseHolder
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.core.module.Module

/**
 * Super-class for koin-based tests.
 *   - Use [setup] and [tearDown] as usual (the annotations are not needed)
 *   - Use [setupKoinModule] to declare additional mocks for koin to inject
 *
 * Beware: For some reason, the timing @MockK annotation does not work properly in this super-class.
 * Properties initialized by it can be used in the sub-class without problems but not in the [baseSetup]
 * of this base-class.
 */
@ExperimentalCoroutinesApi
@ExtendWith(MockKExtension::class)
abstract class RepositoryTestBase : TestBase() {
    private lateinit var databaseHolder: DatabaseHolder
    private lateinit var database: AppDatabase

    @MockK
    protected lateinit var contactDao: ContactDao

    @MockK
    protected lateinit var contactDataDao: ContactDataDao

    @MockK
    protected lateinit var contactGroupDao: ContactGroupDao

    @MockK
    protected lateinit var contactGroupRelationDao: ContactGroupRelationDao

    @MockK
    protected lateinit var contactImageDao: ContactImageDao

    override fun setupKoinModule(module: Module) {
        super.setupKoinModule(module)
        module.single { databaseHolder }
    }

    override fun setup() {
        super.setup()

        databaseHolder = mockk()
        database = mockk()
        coEvery { databaseHolder.ensureInitialized() } returns Unit
        every { databaseHolder.database } returns database
        every { database.contactDao() } returns contactDao
        every { database.contactDataDao() } returns contactDataDao
        every { database.contactGroupDao() } returns contactGroupDao
        every { database.contactGroupRelationDao() } returns contactGroupRelationDao
        every { database.contactImageDao() } returns contactImageDao
    }
}
