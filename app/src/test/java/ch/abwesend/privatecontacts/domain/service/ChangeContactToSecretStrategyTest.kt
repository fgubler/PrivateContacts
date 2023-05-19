/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.service

import ch.abwesend.privatecontacts.domain.model.contactgroup.ContactGroup
import ch.abwesend.privatecontacts.domain.model.result.ContactSaveResult
import ch.abwesend.privatecontacts.domain.repository.IContactGroupRepository
import ch.abwesend.privatecontacts.testutil.TestBase
import ch.abwesend.privatecontacts.testutil.databuilders.someContactEditable
import ch.abwesend.privatecontacts.testutil.databuilders.someContactGroup
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.core.module.Module

@ExperimentalCoroutinesApi
@ExtendWith(MockKExtension::class)
class ChangeContactToSecretStrategyTest : TestBase() {
    @MockK
    private lateinit var contactGroupRepository: IContactGroupRepository

    private val underTest = ChangeContactToSecretStrategy

    override fun setupKoinModule(module: Module) {
        super.setupKoinModule(module)
        module.single { contactGroupRepository }
    }

    @Test
    fun `should create missing contact-groups`() {
        val contacts = listOf(
            someContactEditable(
                contactGroups = listOf(
                    someContactGroup(name = "1.1"),
                    someContactGroup(name = "1.2"),
                ),
            ),
            someContactEditable(
                contactGroups = listOf(
                    someContactGroup(name = "2.1"),
                    someContactGroup(name = "2.2"),
                ),
            ),
        )
        coEvery { contactGroupRepository.createMissingContactGroups(any()) } returns ContactSaveResult.Success

        runBlocking { underTest.createContactGroups(contacts) }

        val capturedContactGroups = slot<List<ContactGroup>>()
        coVerify(exactly = 1) { contactGroupRepository.createMissingContactGroups(capture(capturedContactGroups)) }
        assertThat(capturedContactGroups.isCaptured).isTrue
        assertThat(capturedContactGroups.captured).hasSize(4)
        assertThat(capturedContactGroups.captured).isEqualTo(contacts[0].contactGroups + contacts[1].contactGroups)
    }
}
