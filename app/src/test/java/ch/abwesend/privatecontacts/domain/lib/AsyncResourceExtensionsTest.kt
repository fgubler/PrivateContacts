/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.lib

import ch.abwesend.privatecontacts.domain.lib.flow.AsyncResource
import ch.abwesend.privatecontacts.domain.lib.flow.ErrorResource
import ch.abwesend.privatecontacts.domain.lib.flow.InactiveResource
import ch.abwesend.privatecontacts.domain.lib.flow.LoadingResource
import ch.abwesend.privatecontacts.domain.lib.flow.MutableResourceStateFlow
import ch.abwesend.privatecontacts.domain.lib.flow.ReadyResource
import ch.abwesend.privatecontacts.domain.lib.flow.combineWith
import ch.abwesend.privatecontacts.domain.lib.flow.emitError
import ch.abwesend.privatecontacts.domain.lib.flow.emitInactive
import ch.abwesend.privatecontacts.domain.lib.flow.emitLoading
import ch.abwesend.privatecontacts.domain.lib.flow.emitReady
import ch.abwesend.privatecontacts.domain.lib.flow.toResourceFlow
import ch.abwesend.privatecontacts.domain.lib.flow.withLoadingState
import ch.abwesend.privatecontacts.testutil.TestBase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
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
        super.setup()
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

    @Test
    fun `toResourceFlow should first emit Loading and then ready with the values of the inner flow`() {
        val values = (1..5).toList()
        val innerFlow = flow {
            values.forEach { emit(it) }
        }
        val underTest = innerFlow.toResourceFlow()
        val collector = mockk<suspend (AsyncResource<Int>) -> Unit>(relaxed = true)

        runBlocking { underTest.collect { collector(it) } }

        coVerify(exactly = 1) { collector.invoke(ofType<LoadingResource<Int>>()) }
        values.forEach {
            coVerify(exactly = 1) { collector.invoke(ReadyResource(it)) }
        }
        confirmVerified(collector)
    }

    @Test
    fun `ErrorResource combineWith ErrorResource should return combined ErrorResource`() {
        val error1 = ErrorResource<Int>(listOf(IllegalArgumentException("Test 1")))
        val error2 = ErrorResource<String>(listOf(IllegalStateException("Test 2")))

        val result = error1.combineWith(error2) { _, _ -> }

        assertThat(result).isInstanceOf(ErrorResource::class.java)
        val errors = (result as ErrorResource).errors
        assertThat(errors).hasSize(2)
        assertThat(errors).isEqualTo(error1.errors + error2.errors)
    }

    @Test
    fun `ErrorResource combineWith anything else should return ErrorResource`() {
        val error = ErrorResource<Int>(listOf(IllegalArgumentException("Test 1")))
        val otherResources = listOf(
            LoadingResource<String>(),
            InactiveResource<Boolean>(),
            ReadyResource(0.42),
        )

        val results = otherResources.map { error.combineWith(it) { _, _ -> } }

        results.forEach { result ->
            assertThat(result).isInstanceOf(ErrorResource::class.java)
            assertThat(result).isEqualTo(error)
        }
    }

    @Test
    fun `LoadingResource combineWith lower priority should return LoadingResource`() {
        val loading = LoadingResource<Int>()
        val otherResources = listOf(
            LoadingResource<String>(),
            InactiveResource<Boolean>(),
            ReadyResource(0.42),
        )

        val results = otherResources.map { loading.combineWith(it) { _, _ -> } }

        results.forEach { result ->
            assertThat(result).isInstanceOf(LoadingResource::class.java)
        }
    }

    @Test
    fun `InactiveResource combineWith lower priority should return InactiveResource`() {
        val inactive = InactiveResource<Int>()
        val otherResources = listOf(
            InactiveResource<Boolean>(),
            ReadyResource(0.42),
        )

        val results = otherResources.map { inactive.combineWith(it) { _, _ -> } }

        results.forEach { result ->
            assertThat(result).isInstanceOf(InactiveResource::class.java)
        }
    }

    @Test
    fun `ReadyResource combineWith ReadyResource should return combined ReadyResource`() {
        val ready = ReadyResource(42)
        val otherResource = ReadyResource("The answer is")

        val result = ready.combineWith(otherResource) { thisValue, otherValue ->
            "$otherValue $thisValue"
        }

        assertThat(result).isInstanceOf(ReadyResource::class.java)
        assertThat(result.valueOrNull).isNotNull.isEqualTo("The answer is 42")
    }
}
