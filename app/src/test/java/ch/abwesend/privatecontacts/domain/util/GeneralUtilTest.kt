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
class GeneralUtilTest : TestBase() {
    @Test
    fun `should return the simple name`() {
        val underTest = "Test"

        val simpleName = underTest.simpleClassName

        assertThat(simpleName).isEqualTo("String")
    }

    @Test
    fun `should execute if true`() {
        val testValue = "not changed"

        val result = testValue.doIf(true) { "changed instead of '$it'" }

        assertThat(result).isEqualTo("changed instead of 'not changed'")
    }

    @Test
    fun `should not execute if false`() {
        val testValue = "not changed"

        val result = testValue.doIf(false) { "changed instead of '$it'" }

        assertThat(result).isEqualTo("not changed")
    }
}
