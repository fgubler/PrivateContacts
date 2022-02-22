/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.service

import ch.abwesend.privatecontacts.testutil.TestBase
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith

@ExperimentalCoroutinesApi
@ExtendWith(MockKExtension::class)
class EasterEggServiceTest : TestBase() {
    @InjectMockKs
    private lateinit var underTest: EasterEggService

    @Test
    fun `should crash the app`() {
        val query = EasterEggService.KEYWORD_CRASH
        assertThrows<RuntimeException> { underTest.checkSearchForEasterEggs(query) }
    }
}
