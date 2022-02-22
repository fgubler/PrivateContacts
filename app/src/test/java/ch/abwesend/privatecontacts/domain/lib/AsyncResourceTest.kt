/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.lib

import ch.abwesend.privatecontacts.domain.lib.flow.ErrorHandler
import ch.abwesend.privatecontacts.domain.lib.flow.ErrorResource
import ch.abwesend.privatecontacts.domain.lib.flow.InactiveHandler
import ch.abwesend.privatecontacts.domain.lib.flow.InactiveResource
import ch.abwesend.privatecontacts.domain.lib.flow.LoadingHandler
import ch.abwesend.privatecontacts.domain.lib.flow.LoadingResource
import ch.abwesend.privatecontacts.domain.lib.flow.ReadyHandler
import ch.abwesend.privatecontacts.domain.lib.flow.ReadyResource
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class AsyncResourceTest {
    @Test
    fun `only ReadyResource should react to ifReady`() {
        val value = 42
        val loadingResource = LoadingResource<Int>()
        val readyResource = ReadyResource(value)
        val errorResource = ErrorResource<Int>(emptyList())
        val inactiveResource = InactiveResource<Int>()
        val readyHandler = mockk<ReadyHandler<Int>>()
        every { readyHandler.invoke(any()) } returns Unit

        loadingResource.ifReady(mockk())
        readyResource.ifReady(readyHandler)
        errorResource.ifReady(mockk())
        inactiveResource.ifReady(mockk())

        verify { readyHandler.invoke(value) }
    }

    @Test
    fun `only LoadingResource should react to ifLoading`() {
        val loadingResource = LoadingResource<Int>()
        val readyResource = ReadyResource(42)
        val errorResource = ErrorResource<Int>(emptyList())
        val inactiveResource = InactiveResource<Int>()
        val loadingHandler = mockk<LoadingHandler>()
        every { loadingHandler.invoke() } returns Unit

        loadingResource.ifLoading(loadingHandler)
        readyResource.ifLoading(mockk())
        errorResource.ifLoading(mockk())
        inactiveResource.ifLoading(mockk())

        verify { loadingHandler.invoke() }
    }

    @Test
    fun `only ErrorResource should react to ifError`() {
        val errors = listOf(RuntimeException("Test"))
        val loadingResource = LoadingResource<Int>()
        val readyResource = ReadyResource(42)
        val errorResource = ErrorResource<Int>(errors)
        val inactiveResource = InactiveResource<Int>()
        val errorHandler = mockk<ErrorHandler>()
        every { errorHandler.invoke(any()) } returns Unit

        loadingResource.ifError(mockk())
        readyResource.ifError(mockk())
        errorResource.ifError(errorHandler)
        inactiveResource.ifError(mockk())

        verify { errorHandler.invoke(errors) }
    }

    @Test
    fun `only InactiveResource should react to ifInactive`() {
        val loadingResource = LoadingResource<Int>()
        val readyResource = ReadyResource(42)
        val errorResource = ErrorResource<Int>(emptyList())
        val inactiveResource = InactiveResource<Int>()
        val inactiveHandler = mockk<InactiveHandler>()
        every { inactiveHandler.invoke() } returns Unit

        loadingResource.ifInactive(mockk())
        readyResource.ifInactive(mockk())
        errorResource.ifInactive(mockk())
        inactiveResource.ifInactive(inactiveHandler)

        verify { inactiveHandler.invoke() }
    }
}
