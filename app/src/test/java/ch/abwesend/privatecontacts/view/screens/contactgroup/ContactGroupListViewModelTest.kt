/*
 * Private Contacts
 * Copyright (c) 2026.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.screens.contactgroup

import ch.abwesend.privatecontacts.domain.lib.flow.ReadyResource
import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.model.result.ContactChangeError.UNABLE_TO_DELETE_CONTACT_GROUP
import ch.abwesend.privatecontacts.domain.model.result.ContactChangeError.UNABLE_TO_UPDATE_CONTACT_GROUP
import ch.abwesend.privatecontacts.domain.model.result.ContactDeleteResult
import ch.abwesend.privatecontacts.domain.model.result.ContactSaveResult
import ch.abwesend.privatecontacts.domain.service.ContactGroupService
import ch.abwesend.privatecontacts.domain.settings.ISettingsState
import ch.abwesend.privatecontacts.testutil.TestBase
import ch.abwesend.privatecontacts.testutil.databuilders.someContactGroup
import ch.abwesend.privatecontacts.view.model.ContactGroupWithContactCount
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.core.module.Module

@ExperimentalCoroutinesApi
@ExtendWith(MockKExtension::class)
class ContactGroupListViewModelTest : TestBase() {
    @MockK
    private lateinit var contactGroupService: ContactGroupService

    private lateinit var underTest: ContactGroupListViewModel

    override fun setupKoinModule(module: Module) {
        super.setupKoinModule(module)
        module.single { contactGroupService }
    }

    override fun setup() {
        super.setup()
        Dispatchers.setMain(UnconfinedTestDispatcher())
        underTest = ContactGroupListViewModel()
    }

    override fun tearDown() {
        super.tearDown()
        Dispatchers.resetMain()
    }

    @Test
    fun `should load the secret groups with their number of contacts sorted by name`() {
        val groupB = someContactGroup(name = "b-Group")
        val groupA = someContactGroup(name = "A-Group")
        val emptyGroup = someContactGroup(name = "c-Group")
        coEvery { contactGroupService.loadAllContactGroups(any<ContactType>()) } returns listOf(groupB, groupA, emptyGroup)
        coEvery { contactGroupService.loadNumberOfContactsPerGroup() } returns mapOf("A-Group" to 2, "b-Group" to 1)

        underTest.reloadContactGroups()

        coVerify { contactGroupService.loadAllContactGroups(ContactType.SECRET) }
        assertThat(underTest.contactGroups.value).isEqualTo(
            ReadyResource(
                listOf(
                    ContactGroupWithContactCount(groupA, 2),
                    ContactGroupWithContactCount(groupB, 1),
                    ContactGroupWithContactCount(emptyGroup, 0),
                )
            )
        )
    }

    @Test
    fun `should reload the groups after a successful update`() {
        val oldGroup = someContactGroup(name = "Old name")
        val newGroup = oldGroup.changeName("New name")
        coEvery { contactGroupService.updateContactGroup(any(), any()) } returns ContactSaveResult.Success
        coEvery { contactGroupService.loadAllContactGroups(any<ContactType>()) } returns listOf(newGroup)
        coEvery { contactGroupService.loadNumberOfContactsPerGroup() } returns emptyMap()

        underTest.updateContactGroup(oldGroup, newGroup)

        coVerify { contactGroupService.updateContactGroup(oldGroup, newGroup) }
        coVerify { contactGroupService.loadAllContactGroups(ContactType.SECRET) }
    }

    @Test
    fun `should not reload the groups after a failed update`() {
        val oldGroup = someContactGroup(name = "Old name")
        val newGroup = oldGroup.changeName("New name")
        coEvery { contactGroupService.updateContactGroup(any(), any()) } returns
            ContactSaveResult.Failure(UNABLE_TO_UPDATE_CONTACT_GROUP)

        underTest.updateContactGroup(oldGroup, newGroup)

        coVerify(exactly = 0) { contactGroupService.loadAllContactGroups(any<ContactType>()) }
    }

    @Test
    fun `should reload the groups after a successful deletion`() {
        val group = someContactGroup(name = "Group")
        coEvery { contactGroupService.deleteContactGroup(any()) } returns ContactDeleteResult.Success
        coEvery { contactGroupService.loadAllContactGroups(any<ContactType>()) } returns emptyList()
        coEvery { contactGroupService.loadNumberOfContactsPerGroup() } returns emptyMap()

        underTest.deleteContactGroup(group)

        coVerify { contactGroupService.deleteContactGroup(group) }
        coVerify { contactGroupService.loadAllContactGroups(ContactType.SECRET) }
    }

    @Test
    fun `should not reload the groups after a failed deletion`() {
        val group = someContactGroup(name = "Group")
        coEvery { contactGroupService.deleteContactGroup(any()) } returns
            ContactDeleteResult.Failure(UNABLE_TO_DELETE_CONTACT_GROUP)

        underTest.deleteContactGroup(group)

        coVerify(exactly = 0) { contactGroupService.loadAllContactGroups(any<ContactType>()) }
    }

    @Test
    fun `should select the secret tab on initialization if android contacts are not shown`() {
        val settings = mockk<ISettingsState> { every { showAndroidContacts } returns false }
        coEvery { contactGroupService.loadAllContactGroups(any<ContactType>()) } returns emptyList()
        coEvery { contactGroupService.loadNumberOfContactsPerGroup() } returns emptyMap()
        underTest.selectTab(ContactGroupListTab.PUBLIC_GROUPS)

        underTest.initializeScreen(settings)

        assertThat(underTest.selectedTab.value).isEqualTo(ContactGroupListTab.SECRET_GROUPS)
        coVerify { contactGroupService.loadAllContactGroups(ContactType.SECRET) }
    }

    @Test
    fun `should keep the selected tab on initialization if android contacts are shown`() {
        val settings = mockk<ISettingsState> { every { showAndroidContacts } returns true }
        coEvery { contactGroupService.loadAllContactGroups(any<ContactType>()) } returns emptyList()
        coEvery { contactGroupService.loadNumberOfContactsPerGroup() } returns emptyMap()
        underTest.selectTab(ContactGroupListTab.PUBLIC_GROUPS)

        underTest.initializeScreen(settings)

        assertThat(underTest.selectedTab.value).isEqualTo(ContactGroupListTab.PUBLIC_GROUPS)
    }

    @Test
    fun `should reload the groups after a successful creation`() {
        val group = someContactGroup(name = "New Group")
        coEvery { contactGroupService.createContactGroup(any()) } returns ContactSaveResult.Success
        coEvery { contactGroupService.loadAllContactGroups(any<ContactType>()) } returns listOf(group)
        coEvery { contactGroupService.loadNumberOfContactsPerGroup() } returns emptyMap()

        underTest.createContactGroup(group)

        coVerify { contactGroupService.createContactGroup(group) }
        coVerify { contactGroupService.loadAllContactGroups(ContactType.SECRET) }
    }
}
