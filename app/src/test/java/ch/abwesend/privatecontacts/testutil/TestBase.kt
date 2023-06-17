/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.testutil

import ch.abwesend.privatecontacts.domain.lib.coroutine.IDispatchers
import ch.abwesend.privatecontacts.domain.lib.logging.ILogger
import ch.abwesend.privatecontacts.domain.lib.logging.ILoggerFactory
import ch.abwesend.privatecontacts.domain.settings.SettingsRepository
import ch.abwesend.privatecontacts.domain.util.StringProvider
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.mockkClass
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
import org.koin.test.junit5.mock.MockProviderExtension

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
abstract class TestBase : KoinTest {
    private lateinit var loggerFactory: ILoggerFactory
    private lateinit var testLogger: ILogger

    /**
     * Beware: the [testSettings] are shared between tests running in parallel
     * => should not be changed by individual tests
     */
    private lateinit var testSettings: TestSettings

    /** Sets up the koin-module for injections */
    @RegisterExtension
    @JvmField
    protected val koinTestExtension = KoinTestExtension.create {
        modules(
            module {
                single { loggerFactory }
                single<IDispatchers> { TestDispatchers }
                single<SettingsRepository> { testSettings }
                single<StringProvider> { TestStringProvider() }
                setupKoinModule(this)
            }
        )
    }

    /** Defines that koin-mocks are created with mockk */
    @JvmField
    @RegisterExtension
    protected val mockProvider = MockProviderExtension.create { clazz ->
        mockkClass(clazz)
    }

    @BeforeEach
    fun baseSetup() {
        testSettings = TestSettings()

        loggerFactory = mockk()
        testLogger = TestLogger()
        every { loggerFactory.createDefault(any()) } returns spyk(testLogger)
        every { loggerFactory.createLogcat(any()) } returns spyk(testLogger)

        setup()
    }

    @AfterEach
    fun baseTearDown() {
        tearDown()
    }

    /** executed before each test */
    protected open fun setup() {}

    /** executed after each test */
    protected open fun tearDown() {}

    /** add additional injections to be mocked in koin */
    protected open fun setupKoinModule(module: Module) {}
}
