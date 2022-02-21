/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.service

import androidx.paging.Pager
import ch.abwesend.privatecontacts.domain.model.contact.IContactBase
import ch.abwesend.privatecontacts.domain.model.search.ContactSearchConfig
import ch.abwesend.privatecontacts.domain.repository.ContactPagerFactory
import ch.abwesend.privatecontacts.domain.repository.IContactRepository
import ch.abwesend.privatecontacts.testutil.TestBase
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.core.module.Module

@ExperimentalCoroutinesApi
@ExtendWith(MockKExtension::class)
class ContactLoadServiceTest : TestBase() {
    @MockK
    private lateinit var contactRepository: IContactRepository

    @MockK
    private lateinit var pagerFactory: ContactPagerFactory

    @MockK
    private lateinit var easterEggService: EasterEggService

    @InjectMockKs
    private lateinit var underTest: ContactLoadService

    override fun Module.setupKoinModule() {
        single { contactRepository }
        single { pagerFactory }
        single { easterEggService }
    }

    @Test
    fun `search should check for easter eggs`() {
        val query = "Test"
        val pager = mockk<Pager<Int, IContactBase>>(relaxed = true)
        every { pagerFactory.createContactPager(any()) } returns pager
        every { easterEggService.checkSearchForEasterEggs(any()) } just runs

        underTest.searchContacts(query)

        verify { easterEggService.checkSearchForEasterEggs(query) }
    }

    @Test
    fun `search should pass the query to the paging logic`() {
        val query = "Test"
        val pager = mockk<Pager<Int, IContactBase>>(relaxed = true)
        every { pagerFactory.createContactPager(any()) } returns pager
        every { easterEggService.checkSearchForEasterEggs(any()) } just runs

        underTest.searchContacts(query)

        verify { easterEggService.checkSearchForEasterEggs(query) }
        verify { pagerFactory.createContactPager(ContactSearchConfig.Query(query)) }
        verify { pager.flow }
    }
}
