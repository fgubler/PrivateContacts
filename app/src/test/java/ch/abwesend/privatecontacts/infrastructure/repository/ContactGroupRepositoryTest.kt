/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository

import ch.abwesend.privatecontacts.domain.model.ModelStatus.NEW
import ch.abwesend.privatecontacts.domain.model.ModelStatus.UNCHANGED
import ch.abwesend.privatecontacts.infrastructure.room.contactgroup.ContactGroupEntity
import ch.abwesend.privatecontacts.infrastructure.room.contactgroup.toContactGroup
import ch.abwesend.privatecontacts.infrastructure.room.contactgroup.toEntity
import ch.abwesend.privatecontacts.testutil.RepositoryTestBase
import ch.abwesend.privatecontacts.testutil.databuilders.someContactGroup
import ch.abwesend.privatecontacts.testutil.databuilders.someContactGroupEntity
import ch.abwesend.privatecontacts.testutil.databuilders.someContactGroupRelationEntity
import ch.abwesend.privatecontacts.testutil.databuilders.someContactId
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.runs
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExperimentalCoroutinesApi
@ExtendWith(MockKExtension::class)
class ContactGroupRepositoryTest : RepositoryTestBase() {

    @InjectMockKs
    private lateinit var underTest: ContactGroupRepository

    @Test
    fun `should load corresponding contact-groups`() {
        val contactId = someContactId()
        val contactGroupsEntities = listOf(
            someContactGroupEntity(name = "Group 1", notes = "Notes 1"),
            someContactGroupEntity(name = "Group 2", notes = "Notes 2"),
            someContactGroupEntity(name = "Group 3", notes = "Notes 3"),
        )
        val contactGroupRelationEntities = contactGroupsEntities.map {
            someContactGroupRelationEntity(groupName = it.name, contactId = contactId.uuid)
        }
        coEvery { contactGroupRelationDao.getRelationsForContact(any()) } returns contactGroupRelationEntities
        coEvery { contactGroupDao.getGroups(any()) } returns contactGroupsEntities
        val contactGroupNames = contactGroupsEntities.map { it.name }
        val contactGroups = contactGroupsEntities.map { it.toContactGroup() }

        val result = runBlocking { underTest.getContactGroups(contactId) }

        coVerify { contactGroupRelationDao.getRelationsForContact(contactId.uuid) }
        coVerify { contactGroupDao.getGroups(contactGroupNames) }
        assertThat(result).isEqualTo(contactGroups)
    }

    @Test
    fun `should create missing contact groups and replace relationships`() {
        val contactId = someContactId()
        val existingContactGroups = listOf(
            someContactGroup(name = "Group 1", notes = "Notes 1", modelStatus = UNCHANGED),
            someContactGroup(name = "Group 2", notes = "Notes 2", modelStatus = UNCHANGED),
            someContactGroup(name = "Group 3", notes = "Notes 3", modelStatus = UNCHANGED),
        )
        val newContactGroups = listOf(
            someContactGroup(name = "New Group 1", modelStatus = NEW),
            someContactGroup(name = "New Group 2", modelStatus = NEW),
        )
        val newContactGroupEntities = newContactGroups.map { it.toEntity() }
        val contactGroups = existingContactGroups.take(2) + newContactGroups
        coEvery { contactGroupDao.upsertAll(any()) } just runs
        coEvery { contactGroupRelationDao.deleteRelationsForContact(any()) } just runs
        coEvery { contactGroupRelationDao.insertAll(any()) } just runs

        runBlocking { underTest.storeContactGroups(contactId, contactGroups) }

        coVerify { contactGroupDao.upsertAll(newContactGroupEntities) }
        coVerify { contactGroupRelationDao.deleteRelationsForContact(contactId.uuid) }
        coVerify { contactGroupRelationDao.insertAll(any()) }
    }

    @Test
    fun `should only create missing contact groups`() {
        val existingContactGroups = listOf(
            someContactGroup(name = "Group 1", notes = "Notes 1", modelStatus = UNCHANGED),
            someContactGroup(name = "Group 2", notes = "Notes 2", modelStatus = UNCHANGED),
            someContactGroup(name = "Group 3", notes = "Notes 3", modelStatus = UNCHANGED),
        )
        val newContactGroups = listOf(
            someContactGroup(name = "New Group 1", modelStatus = NEW),
            someContactGroup(name = "New Group 2", modelStatus = NEW),
        )
        val newContactGroupEntities = newContactGroups.map { it.toEntity() }
        val contactGroups = existingContactGroups.take(2) + newContactGroups

        coEvery { contactGroupDao.upsertAll(any()) } just runs

        runBlocking { underTest.createMissingContactGroups(contactGroups) }

        val capturedGroups = slot<List<ContactGroupEntity>>()
        coVerify { contactGroupDao.upsertAll(capture(capturedGroups)) }
        assertThat(capturedGroups.isCaptured).isTrue
        assertThat(capturedGroups.captured).isEqualTo(newContactGroupEntities)
    }

    @Test
    fun `should replace the existing group-relations with the new ones`() {
        val contactId = someContactId()
        val existingContactGroups = listOf(
            someContactGroup(name = "Group 1", notes = "Notes 1", modelStatus = UNCHANGED),
            someContactGroup(name = "Group 2", notes = "Notes 2", modelStatus = UNCHANGED),
            someContactGroup(name = "Group 3", notes = "Notes 3", modelStatus = UNCHANGED),
        )
        val newContactGroups = listOf(
            someContactGroup(name = "New Group 1", modelStatus = NEW),
            someContactGroup(name = "New Group 2", modelStatus = NEW),
        )
        val contactGroups = existingContactGroups.take(2) + newContactGroups
        val contactGroupRelations = contactGroups.map {
            someContactGroupRelationEntity(contactId = contactId.uuid, groupName = it.id.name)
        }
        coEvery { contactGroupDao.upsertAll(any()) } just runs
        coEvery { contactGroupRelationDao.deleteRelationsForContact(any()) } just runs
        coEvery { contactGroupRelationDao.insertAll(any()) } just runs

        runBlocking { underTest.storeContactGroups(contactId, contactGroups) }

        coVerify { contactGroupRelationDao.deleteRelationsForContact(contactId.uuid) }
        coVerify { contactGroupRelationDao.insertAll(contactGroupRelations) }
    }
}
