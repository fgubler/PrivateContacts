/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.lib

import ch.abwesend.privatecontacts.domain.lib.flow.ErrorResource
import ch.abwesend.privatecontacts.domain.lib.flow.InactiveResource
import ch.abwesend.privatecontacts.domain.lib.flow.LoadingResource
import ch.abwesend.privatecontacts.domain.lib.flow.MutableResourceStateFlow
import ch.abwesend.privatecontacts.domain.lib.flow.ReadyResource
import ch.abwesend.privatecontacts.domain.lib.flow.emitError
import ch.abwesend.privatecontacts.domain.lib.flow.emitInactive
import ch.abwesend.privatecontacts.domain.lib.flow.emitLoading
import ch.abwesend.privatecontacts.domain.lib.flow.emitReady
import ch.abwesend.privatecontacts.domain.lib.flow.withLoadingState
import ch.abwesend.privatecontacts.testutil.TestBase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExperimentalCoroutinesApi
@ExtendWith(MockKExtension::class)
class AsyncResourceExtensionsTest : TestBase() {
    @MockK
    private lateinit var flow: MutableResourceStateFlow<Int>

    override fun setup() {
        coEvery { flow.emit(any()) } returns Unit
    }

    @Test
    fun `emitLoading should emit a LoadingResource`() {
        runBlocking { flow.emitLoading() }
        coVerify { flow.emit(match { it is LoadingResource }) }
        confirmVerified(flow)
    }

    @Test
    fun `emitInactive should emit an InactiveResource`() {
        runBlocking { flow.emitInactive() }
        coVerify { flow.emit(match { it is InactiveResource }) }
        confirmVerified(flow)
    }

    @Test
    fun `emitReady should emit a ReadyResource`() {
        val value = 42

        runBlocking { flow.emitReady(value) }

        coVerify { flow.emit(ReadyResource(value)) }
        confirmVerified(flow)
    }

    @Test
    fun `emitError should emit an ErrorResource`() {
        val error = RuntimeException("Test")

        runBlocking { flow.emitError(error) }

        coVerify { flow.emit(ErrorResource(listOf(error))) }
        confirmVerified(flow)
    }

    @Test
    fun `withLoadingState should first emit Loading and then Ready if successful`() {
        val value = 42

        val result = runBlocking {
            flow.withLoadingState { value }
        }

        coVerify(exactly = 1) { flow.emit(match { it is LoadingResource }) }
        coVerify(exactly = 1) { flow.emit(ReadyResource(value)) }
        confirmVerified(flow)
        assertThat(result).isEqualTo(value)
    }

    @Test
    fun `withLoadingState should first emit Loading and then Error if not successful`() {
        val error = RuntimeException("Test")

        val result = runBlocking {
            flow.withLoadingState { throw error }
        }

        coVerify(exactly = 1) { flow.emit(match { it is LoadingResource }) }
        coVerify(exactly = 1) { flow.emit(ErrorResource(listOf(error))) }
        confirmVerified(flow)
        assertThat(result).isNull()
    }
}
