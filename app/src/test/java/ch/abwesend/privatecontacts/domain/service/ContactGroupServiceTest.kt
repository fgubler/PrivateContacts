/*
 * Private Contacts
 * Copyright (c) 2026.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.service

import ch.abwesend.privatecontacts.domain.model.result.ContactChangeError.UNABLE_TO_DELETE_CONTACT_GROUP
import ch.abwesend.privatecontacts.domain.model.result.ContactChangeError.UNABLE_TO_SAVE_CONTACT
import ch.abwesend.privatecontacts.domain.model.result.ContactChangeError.UNABLE_TO_UPDATE_CONTACT_GROUP
import ch.abwesend.privatecontacts.domain.model.result.ContactDeleteResult
import ch.abwesend.privatecontacts.domain.model.result.ContactSaveResult
import ch.abwesend.privatecontacts.domain.repository.IAndroidContactLoadService
import ch.abwesend.privatecontacts.domain.repository.IContactGroupRepository
import ch.abwesend.privatecontacts.domain.repository.IContactRepository
import ch.abwesend.privatecontacts.testutil.TestBase
import ch.abwesend.privatecontacts.testutil.databuilders.someContactGroup
import ch.abwesend.privatecontacts.testutil.databuilders.someContactId
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.confirmVerified
import io.mockk.impl.annotations.InjectMockKs
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
class ContactGroupServiceTest : TestBase() {
    @MockK
    private lateinit var contactGroupRepository: IContactGroupRepository

    @MockK
    private lateinit var contactRepository: IContactRepository

    @MockK
    private lateinit var androidContactService: IAndroidContactLoadService

    @InjectMockKs
    private lateinit var underTest: ContactGroupService

    override fun setupKoinModule(module: Module) {
        super.setupKoinModule(module)
        module.single { contactGroupRepository }
        module.single { contactRepository }
        module.single { androidContactService }
    }

    @Test
    fun `should load the number of contacts per group from the repository`() {
        val numberOfContacts = mapOf("Group 1" to 2, "Group 2" to 0)
        coEvery { contactGroupRepository.loadNumberOfContactsPerGroup() } returns numberOfContacts

        val result = runBlocking { underTest.loadNumberOfContactsPerGroup() }

        assertThat(result).isEqualTo(numberOfContacts)
    }

    @Test
    fun `should not re-compute the full-text-search if only the notes changed`() {
        val oldGroup = someContactGroup(name = "Group", notes = "Old notes")
        val newGroup = oldGroup.changeNotes("New notes")
        coEvery { contactGroupRepository.updateContactGroup(any(), any()) } returns ContactSaveResult.Success

        val result = runBlocking { underTest.updateContactGroup(oldGroup, newGroup) }

        assertThat(result).isEqualTo(ContactSaveResult.Success)
        coVerify { contactGroupRepository.updateContactGroup(oldGroup, newGroup) }
        confirmVerified(contactGroupRepository, contactRepository)
    }

    @Test
    fun `should re-compute the full-text-search of the contacts in a renamed group`() {
        val oldGroup = someContactGroup(name = "Old name")
        val newGroup = oldGroup.changeName("New name")
        val contactIds = setOf(someContactId(), someContactId())
        coEvery { contactGroupRepository.updateContactGroup(any(), any()) } returns ContactSaveResult.Success
        coEvery { contactGroupRepository.getContactIdsInGroups(any()) } returns contactIds
        coEvery { contactRepository.recomputeFullTextSearch(any()) } returns ContactSaveResult.Success

        val result = runBlocking { underTest.updateContactGroup(oldGroup, newGroup) }

        assertThat(result).isEqualTo(ContactSaveResult.Success)
        coVerifyOrder {
            contactGroupRepository.updateContactGroup(oldGroup, newGroup)
            contactGroupRepository.getContactIdsInGroups(listOf("New name"))
            contactRepository.recomputeFullTextSearch(contactIds)
        }
    }

    @Test
    fun `should not re-compute the full-text-search if the rename failed`() {
        val oldGroup = someContactGroup(name = "Old name")
        val newGroup = oldGroup.changeName("New name")
        val failure = ContactSaveResult.Failure(UNABLE_TO_UPDATE_CONTACT_GROUP)
        coEvery { contactGroupRepository.updateContactGroup(any(), any()) } returns failure

        val result = runBlocking { underTest.updateContactGroup(oldGroup, newGroup) }

        assertThat(result).isEqualTo(failure)
        coVerify { contactGroupRepository.updateContactGroup(oldGroup, newGroup) }
        confirmVerified(contactGroupRepository, contactRepository)
    }

    @Test
    fun `should report a successful rename even if the full-text-search could not be re-computed`() {
        val oldGroup = someContactGroup(name = "Old name")
        val newGroup = oldGroup.changeName("New name")
        coEvery { contactGroupRepository.updateContactGroup(any(), any()) } returns ContactSaveResult.Success
        coEvery { contactGroupRepository.getContactIdsInGroups(any()) } returns setOf(someContactId())
        coEvery { contactRepository.recomputeFullTextSearch(any()) } returns
            ContactSaveResult.Failure(UNABLE_TO_SAVE_CONTACT)

        val result = runBlocking { underTest.updateContactGroup(oldGroup, newGroup) }

        assertThat(result).isEqualTo(ContactSaveResult.Success)
    }

    @Test
    fun `should re-compute the full-text-search of the former members of a deleted group`() {
        val group = someContactGroup(name = "Group")
        val contactIds = setOf(someContactId(), someContactId())
        coEvery { contactGroupRepository.getContactIdsInGroups(any()) } returns contactIds
        coEvery { contactGroupRepository.deleteContactGroup(any()) } returns ContactDeleteResult.Success
        coEvery { contactRepository.recomputeFullTextSearch(any()) } returns ContactSaveResult.Success

        val result = runBlocking { underTest.deleteContactGroup(group) }

        assertThat(result).isEqualTo(ContactDeleteResult.Success)
        coVerifyOrder {
            contactGroupRepository.getContactIdsInGroups(listOf("Group"))
            contactGroupRepository.deleteContactGroup(group)
            contactRepository.recomputeFullTextSearch(contactIds)
        }
    }

    @Test
    fun `should not re-compute the full-text-search if the deletion failed`() {
        val group = someContactGroup(name = "Group")
        val failure = ContactDeleteResult.Failure(UNABLE_TO_DELETE_CONTACT_GROUP)
        coEvery { contactGroupRepository.getContactIdsInGroups(any()) } returns setOf(someContactId())
        coEvery { contactGroupRepository.deleteContactGroup(any()) } returns failure

        val result = runBlocking { underTest.deleteContactGroup(group) }

        assertThat(result).isEqualTo(failure)
        coVerify(exactly = 0) { contactRepository.recomputeFullTextSearch(any()) }
    }
}
