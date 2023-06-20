/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.util

import ch.abwesend.privatecontacts.testutil.TestBase
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExperimentalCoroutinesApi
@ExtendWith(MockKExtension::class)
class CollectionUtilTest : TestBase() {
    @Test
    fun `should remove null keys but leave the others untouched`() {
        val testMap: Map<String?, Boolean> = mapOf(
            null to false,
            "Test" to true,
            "Other" to true,
        )
        val notNullKeys = testMap.keys.filterNotNull()

        val result = testMap.filterKeysNotNull()

        assertThat(result).hasSize(2)
        assertThat(result.keys).containsExactlyInAnyOrder(*notNullKeys.toTypedArray())
        assertThat(result.all { it.value }).isTrue
        result.forEach { assertThat(it.key).isNotNull() }
    }

    @Test
    fun `should remove null values but leave the others untouched`() {
        val testMap: Map<String, Boolean?> = mapOf(
            "Null" to null,
            "Test" to true,
            "Other" to false,
        )
        val notNullValues = testMap.values.filterNotNull()

        val result = testMap.filterValuesNotNull()

        assertThat(result).hasSize(2)
        assertThat(result.values).containsExactlyInAnyOrder(*notNullValues.toTypedArray())

        result.forEach { assertThat(it.value).isNotNull() }
    }
}
