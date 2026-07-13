/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.util

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.UUID

class DeterministicUuidTest {
    @Test
    fun `should be stable for the same input`() {
        val first = deterministicUuid("contact-guid", "command-id")
        val second = deterministicUuid("contact-guid", "command-id")
        assertThat(first).isEqualTo(second)
    }

    @Test
    fun `should be case-insensitive`() {
        assertThat(deterministicUuid("ABC")).isEqualTo(deterministicUuid("abc"))
    }

    @Test
    fun `should be order-sensitive`() {
        assertThat(deterministicUuid("a", "b")).isNotEqualTo(deterministicUuid("b", "a"))
    }

    @Test
    fun `should match the pinned byte-encoding`() {
        val expected = UUID.nameUUIDFromBytes("a|b".toByteArray(Charsets.UTF_8))
        assertThat(deterministicUuid("a", "b")).isEqualTo(expected)
    }
}
