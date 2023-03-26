/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model

import ch.abwesend.privatecontacts.domain.model.ModelStatus.CHANGED
import ch.abwesend.privatecontacts.domain.model.ModelStatus.DELETED
import ch.abwesend.privatecontacts.domain.model.ModelStatus.NEW
import ch.abwesend.privatecontacts.domain.model.ModelStatus.UNCHANGED
import ch.abwesend.privatecontacts.testutil.TestBase
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

data class TestWithModelStatus(override val modelStatus: ModelStatus) : WithModelStatus

@ExperimentalCoroutinesApi
@ExtendWith(MockKExtension::class)
class ModelStatusTest : TestBase() {
    @Test
    fun `should only return true for NEW and CHANGED`() {
        val statuses = listOf(
            TestWithModelStatus(modelStatus = NEW),
            TestWithModelStatus(modelStatus = CHANGED),
            TestWithModelStatus(modelStatus = DELETED),
            TestWithModelStatus(modelStatus = UNCHANGED),
        )

        val results = statuses.map { it.modelStatus.isChanged }

        assertThat(results[0]).isTrue
        assertThat(results[1]).isTrue
        assertThat(results[2]).isFalse
        assertThat(results[3]).isFalse
    }

    @Test
    fun `should filter for NEW and CHANGED`() {
        val statuses = listOf(
            TestWithModelStatus(modelStatus = NEW),
            TestWithModelStatus(modelStatus = CHANGED),
            TestWithModelStatus(modelStatus = DELETED),
            TestWithModelStatus(modelStatus = UNCHANGED),
        )

        val results = statuses.filterForChanged()

        assertThat(results).hasSize(2)
        assertThat(results[0]).isEqualTo(statuses[0])
        assertThat(results[1]).isEqualTo(statuses[1])
    }
}
