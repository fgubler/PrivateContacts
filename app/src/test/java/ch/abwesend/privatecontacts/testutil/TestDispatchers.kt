/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.testutil

import ch.abwesend.privatecontacts.domain.lib.coroutine.IDispatchers
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher

/**
 * Beware: do not use [StandardTestDispatcher].
 * Otherwise, the build will hang itself during the 56t test (whichever that may be).
 */
@ExperimentalCoroutinesApi
object TestDispatchers : IDispatchers {
    override val default: CoroutineDispatcher = UnconfinedTestDispatcher()
    override val io: CoroutineDispatcher = UnconfinedTestDispatcher()
    override val main: CoroutineDispatcher = UnconfinedTestDispatcher()
    override val mainImmediate: CoroutineDispatcher = UnconfinedTestDispatcher()
}
