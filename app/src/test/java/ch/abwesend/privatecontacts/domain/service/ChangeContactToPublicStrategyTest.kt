/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.service

import ch.abwesend.privatecontacts.domain.model.contact.ContactAccount
import ch.abwesend.privatecontacts.domain.model.contactdata.IContactDataIdExternal
import ch.abwesend.privatecontacts.domain.model.contactdata.IContactDataIdInternal
import ch.abwesend.privatecontacts.domain.model.contactgroup.ContactGroup
import ch.abwesend.privatecontacts.domain.model.result.ContactSaveResult
import ch.abwesend.privatecontacts.domain.repository.IAndroidContactSaveService
import ch.abwesend.privatecontacts.testutil.TestBase
import ch.abwesend.privatecontacts.testutil.databuilders.someContactEditable
import ch.abwesend.privatecontacts.testutil.databuilders.someContactGroup
import ch.abwesend.privatecontacts.testutil.databuilders.someListOfContactData
import ch.abwesend.privatecontacts.testutil.databuilders.someOnlineAccount
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.core.module.Module

@ExperimentalCoroutinesApi
@ExtendWith(MockKExtension::class)
class ChangeContactToPublicStrategyTest : TestBase() {
    @MockK
    private lateinit var contactSaveService: IAndroidContactSaveService

    private val underTest = ChangeContactToPublicStrategy

    override fun setupKoinModule(module: Module) {
        super.setupKoinModule(module)
        module.single { contactSaveService }
    }

    @Test
    fun `should create missing contact-groups in the correct account`() {
        val accounts = listOf(
            someOnlineAccount(username = "A", provider = "B"),
            someOnlineAccount(username = "C", provider = "D"),
        )
        val contacts = listOf(
            someContactEditable(
                saveInAccount = accounts[0],
                contactGroups = listOf(
                    someContactGroup(name = "1.1"),
                    someContactGroup(name = "1.2"),
                ),
            ),
            someContactEditable(
                saveInAccount = accounts[0],
                contactGroups = listOf(
                    someContactGroup(name = "2.1"),
                    someContactGroup(name = "2.2"),
                ),
            ),
            someContactEditable(
                saveInAccount = accounts[1],
                contactGroups = listOf(
                    someContactGroup(name = "3.1"),
                    someContactGroup(name = "3.2"),
                ),
            ),
        )
        coEvery { contactSaveService.createMissingContactGroups(any(), any()) } returns ContactSaveResult.Success

        runBlocking { underTest.createContactGroups(contacts) }

        val capturedAccounts = mutableListOf<ContactAccount>()
        val capturedContactGroups = mutableListOf<List<ContactGroup>>()
        coVerify(exactly = accounts.size) {
            contactSaveService.createMissingContactGroups(capture(capturedAccounts), capture(capturedContactGroups))
        }
        assertThat(capturedAccounts).hasSize(2)
        assertThat(capturedAccounts).isEqualTo(accounts)
        assertThat(capturedContactGroups).hasSize(2)
        assertThat(capturedContactGroups[0]).hasSize(4)
        assertThat(capturedContactGroups[0]).isEqualTo(contacts[0].contactGroups + contacts[1].contactGroups)
        assertThat(capturedContactGroups[1]).hasSize(2)
        assertThat(capturedContactGroups[1]).isEqualTo(contacts[2].contactGroups)
    }

    @Test
    fun `should change contact-data-IDs to external`() {
        val originalContactData = someListOfContactData()
        val contact = someContactEditable(contactData = originalContactData)

        underTest.changeContactDataIds(contact)

        val newContactData = contact.contactDataSet
        assertThat(originalContactData.all { it.id is IContactDataIdInternal }).isTrue // check test-setup
        assertThat(newContactData.all { it.id is IContactDataIdExternal }).isTrue
    }
}
