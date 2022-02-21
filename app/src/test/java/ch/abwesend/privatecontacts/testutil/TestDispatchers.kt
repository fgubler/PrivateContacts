/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.testutil

import ch.abwesend.privatecontacts.domain.lib.coroutine.IDispatchers
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher

@ExperimentalCoroutinesApi
object TestDispatchers : IDispatchers {
    override val default: CoroutineDispatcher = TestCoroutineDispatcher()
    override val io: CoroutineDispatcher = TestCoroutineDispatcher()
    override val main: CoroutineDispatcher = TestCoroutineDispatcher()
    override val mainImmediate: CoroutineDispatcher = TestCoroutineDispatcher()
}
