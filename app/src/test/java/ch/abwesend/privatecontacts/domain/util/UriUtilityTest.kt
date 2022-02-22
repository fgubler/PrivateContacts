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
class UriUtilityTest : TestBase() {
    @Test
    fun `should add protocol if it is missing (or invalid)`() {
        val protocol = "https"
        val uris = listOf(
            "www.google.ch",
            "google.ch",
            "mail.google.ch",
            "somethingRandom",
            "://google.ch",
            "http:/google.ch",
            "http//google.ch",
            "http:google.ch",
        )

        val results = uris.map { addProtocolToUriString(it, protocol) }

        assertThat(results).hasSameSizeAs(uris)
        results.forEachIndexed { index, result ->
            assertThat(result).startsWith(protocol)
            assertThat(result).isEqualTo("$protocol://${uris[index]}")
        }
    }

    @Test
    fun `should do nothing if the uri already contains a protocol`() {
        val protocol = "https"
        val uris = listOf(
            "http://www.google.ch",
            "https://www.google.ch",
            "ftp://www.google.ch",
            "smtp://www.google.ch",
        )

        val results = uris.map { addProtocolToUriString(it, protocol) }

        assertThat(results).hasSameSizeAs(uris)
        results.forEachIndexed { index, result ->
            assertThat(result).isEqualTo(uris[index])
        }
    }
}
