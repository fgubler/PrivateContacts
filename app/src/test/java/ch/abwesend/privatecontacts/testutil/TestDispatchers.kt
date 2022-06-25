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

@ExperimentalCoroutinesApi
object TestDispatchers : IDispatchers {
    override val default: CoroutineDispatcher = StandardTestDispatcher()
    override val io: CoroutineDispatcher = StandardTestDispatcher()
    override val main: CoroutineDispatcher = StandardTestDispatcher()
    override val mainImmediate: CoroutineDispatcher = StandardTestDispatcher()
}
