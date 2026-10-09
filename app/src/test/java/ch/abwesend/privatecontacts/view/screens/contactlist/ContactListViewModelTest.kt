/*
 * Private Contacts
 * Copyright (c) 2026.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.screens.contactlist

import ch.abwesend.privatecontacts.domain.lib.flow.ReadyResource
import ch.abwesend.privatecontacts.domain.model.contact.ContactIdInternal
import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.service.ContactGroupService
import ch.abwesend.privatecontacts.domain.service.ContactLoadService
import ch.abwesend.privatecontacts.testutil.TestBase
import ch.abwesend.privatecontacts.testutil.databuilders.someContactBase
import ch.abwesend.privatecontacts.testutil.databuilders.someContactGroup
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.core.module.Module

@ExperimentalCoroutinesApi
@ExtendWith(MockKExtension::class)
class ContactListViewModelTest : TestBase() {
    @MockK
    private lateinit var loadService: ContactLoadService

    @MockK
    private lateinit var contactGroupService: ContactGroupService

    private lateinit var underTest: ContactListViewModel

    override fun setupKoinModule(module: Module) {
        super.setupKoinModule(module)
        module.single { loadService }
        module.single { contactGroupService }
    }

    override fun setup() {
        super.setup()
        Dispatchers.setMain(UnconfinedTestDispatcher())
        underTest = ContactListViewModel()
    }

    override fun tearDown() {
        super.tearDown()
        Dispatchers.resetMain()
    }

    @Test
    fun `should remove groups from the filter which do not exist anymore`() {
        val contacts = listOf(someContactBase(firstName = "Anna"), someContactBase(firstName = "Bob"))
        coEvery { loadService.loadSecretContacts() } returns flowOf(ReadyResource(contacts))
        coEvery { contactGroupService.loadAllContactGroups(any<ContactType>()) } returns
            listOf(someContactGroup(name = "Renamed Group"))

        underTest.toggleContactGroupFilter("Old Group")

        assertThat(underTest.contactGroupFilter.value).isEmpty()
        assertThat(underTest.contacts.value).isEqualTo(ReadyResource(contacts))
        coVerify(exactly = 0) { contactGroupService.getContactIdsInGroups(any()) }
    }

    @Test
    fun `should keep existing groups in the filter and filter the contacts by them`() {
        val contactInGroup = someContactBase(firstName = "Anna")
        val otherContact = someContactBase(firstName = "Bob")
        coEvery { loadService.loadSecretContacts() } returns flowOf(ReadyResource(listOf(contactInGroup, otherContact)))
        coEvery { contactGroupService.loadAllContactGroups(any<ContactType>()) } returns
            listOf(someContactGroup(name = "Group"), someContactGroup(name = "Other Group"))
        coEvery { contactGroupService.getContactIdsInGroups(any()) } returns
            setOf(contactInGroup.id as ContactIdInternal)

        underTest.toggleContactGroupFilter("Group")

        assertThat(underTest.contactGroupFilter.value).containsExactly("Group")
        assertThat(underTest.contacts.value).isEqualTo(ReadyResource(listOf(contactInGroup)))
        coVerify { contactGroupService.loadAllContactGroups(ContactType.SECRET) }
        coVerify { contactGroupService.getContactIdsInGroups(setOf("Group")) }
    }

    @Test
    fun `should not load the contact-groups without an active filter`() {
        val contacts = listOf(someContactBase())
        coEvery { loadService.loadSecretContacts() } returns flowOf(ReadyResource(contacts))

        underTest.reloadContacts()

        assertThat(underTest.contacts.value).isEqualTo(ReadyResource(contacts))
        coVerify(exactly = 0) { contactGroupService.loadAllContactGroups(any<ContactType>()) }
    }
}
