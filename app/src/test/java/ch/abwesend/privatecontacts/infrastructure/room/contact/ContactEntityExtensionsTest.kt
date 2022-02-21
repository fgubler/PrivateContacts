/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.room.contact

import ch.abwesend.privatecontacts.domain.service.FullTextSearchService
import ch.abwesend.privatecontacts.testutil.TestBase
import ch.abwesend.privatecontacts.testutil.someContactFull
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.core.module.Module

@ExperimentalCoroutinesApi
@ExtendWith(MockKExtension::class)
class ContactEntityExtensionsTest : TestBase() {
    @MockK
    private lateinit var searchService: FullTextSearchService

    override fun Module.setupKoinModule() {
        single { searchService }
    }

    @Test
    fun `should compute full text search`() {
        val contact = someContactFull()
        val fullText = "TestFullText"
        every { searchService.computeFullTextSearchColumn(any()) } returns fullText

        val entity = contact.toEntity()

        verify { searchService.computeFullTextSearchColumn(contact) }
        assertThat(entity.fullTextSearch).isEqualTo(fullText)
    }
}
