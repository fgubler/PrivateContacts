/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository

import ch.abwesend.privatecontacts.infrastructure.room.database.AppDatabase
import ch.abwesend.privatecontacts.testutil.RepositoryTestBase
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

private class TestRepository : RepositoryBase() {
    suspend fun <T> testBulkOperation(
        data: Collection<Int>,
        operation: suspend (AppDatabase, Collection<Int>) -> T
    ) = bulkOperation(data, operation)
}

@ExperimentalCoroutinesApi
@ExtendWith(MockKExtension::class)
class RepositoryBaseTest : RepositoryTestBase() {
    private lateinit var underTest: TestRepository

    override fun setup() {
        super.setup()
        underTest = TestRepository()
    }

    @Test
    fun `a bulk-operation with few elements should not be split`() {
        val elements = (1 until 200).toList()
        val operation = mockk<suspend (AppDatabase, Collection<Int>) -> Unit>(relaxed = true)

        runBlocking { underTest.testBulkOperation(elements, operation) }

        coVerify(exactly = 1) { operation(any(), elements) }
        confirmVerified(operation)
    }

    @Test
    fun `a bulk-operation with few elements should return one result`() {
        var counter = 0
        val elements = (1 until 200).toList()
        val operation: suspend (AppDatabase, Collection<Int>) -> Int = { _, _ ->
            counter++
        }

        val result = runBlocking { underTest.testBulkOperation(elements, operation) }

        assertThat(result).hasSize(1)
        assertThat(result.first()).isEqualTo(0)
    }

    @Test
    fun `a bulk-operation with too many elements should be split`() {
        val numberOfElements = (2.1 * MAX_BULK_OPERATION_SIZE).toInt()
        val elements = (1 until numberOfElements).toList()
        val operation = mockk<suspend (AppDatabase, Collection<Int>) -> Unit>(relaxed = true)

        runBlocking { underTest.testBulkOperation(elements, operation) }

        coVerify(exactly = 3) { operation(any(), any()) }
        confirmVerified(operation)
    }

    @Test
    fun `a bulk-operation with too many elements should return multiple results`() {
        var counter = 0
        val numberOfElements = (2.1 * MAX_BULK_OPERATION_SIZE).toInt()
        val elements = (1 until numberOfElements).toList()
        val operation: suspend (AppDatabase, Collection<Int>) -> Int = { _, _ ->
            counter++
        }

        val result = runBlocking { underTest.testBulkOperation(elements, operation) }

        assertThat(result).hasSize(3)
        assertThat(result).isEqualTo(listOf(0, 1, 2))
    }
}
