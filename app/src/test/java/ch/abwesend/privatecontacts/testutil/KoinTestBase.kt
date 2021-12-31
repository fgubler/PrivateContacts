package ch.abwesend.privatecontacts.testutil

import ch.abwesend.privatecontacts.domain.lib.coroutine.IDispatchers
import ch.abwesend.privatecontacts.domain.lib.logging.ILoggerFactory
import ch.abwesend.privatecontacts.domain.lib.logging.LogcatLogger
import ch.abwesend.privatecontacts.infrastructure.room.contact.ContactDao
import ch.abwesend.privatecontacts.infrastructure.room.contactdata.ContactDataDao
import ch.abwesend.privatecontacts.infrastructure.room.database.AppDatabase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.junit5.KoinTestExtension

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
abstract class KoinTestBase : KoinTest {
    private lateinit var loggerFactory: ILoggerFactory
    private lateinit var database: AppDatabase

    @MockK
    protected lateinit var contactDao: ContactDao

    @MockK
    protected lateinit var contactDataDao: ContactDataDao

    @RegisterExtension
    @JvmField
    val koinTestExtension = KoinTestExtension.create {
        modules(
            module {
                single { loggerFactory }
                single { database }
                single<IDispatchers> { TestDispatchers }
                setupKoinModule()
            }
        )
    }

    @BeforeEach
    fun baseSetup() {
        loggerFactory = mockk()
        every { loggerFactory.createDefault(any()) } returns spyk(LogcatLogger("Test", false))
        every { loggerFactory.createLogcat(any()) } returns spyk(LogcatLogger("Test", false))

        database = mockk()
        coEvery { database.ensureInitialized() } returns Unit
        every { database.contactDao() } returns contactDao
        every { database.contactDataDao() } returns contactDataDao

        setup()
    }

    @AfterEach
    fun baseTearDown() {
        tearDown()
    }

    protected open fun setup() {}
    protected open fun tearDown() {}
    protected open fun Module.setupKoinModule() {}
}
