/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository

import ch.abwesend.privatecontacts.infrastructure.room.contactgroup.toContactGroup
import ch.abwesend.privatecontacts.testutil.RepositoryTestBase
import ch.abwesend.privatecontacts.testutil.databuilders.someContactGroupEntity
import ch.abwesend.privatecontacts.testutil.databuilders.someContactGroupRelationEntity
import ch.abwesend.privatecontacts.testutil.databuilders.someContactId
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.runs
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
    fun `should create missing contact groups`() {
        val contactId = someContactId()
        val existingContactGroupsEntities = listOf(
            someContactGroupEntity(name = "Group 1", notes = "Notes 1"),
            someContactGroupEntity(name = "Group 2", notes = "Notes 2"),
            someContactGroupEntity(name = "Group 3", notes = "Notes 3"),
        )
        val newContactGroupEntities = listOf(
            someContactGroupEntity(name = "New Group 1"),
            someContactGroupEntity(name = "New Group 2"),
        )
        val existingContactGroupNames = existingContactGroupsEntities.map { it.name }
        val contactGroupEntities = existingContactGroupsEntities.take(2) + newContactGroupEntities
        coEvery { contactGroupDao.getGroupNames() } returns existingContactGroupNames
        coEvery { contactGroupDao.insertAll(any()) } just runs
        coEvery { contactGroupRelationDao.deleteRelationsForContact(any()) } just runs
        coEvery { contactGroupRelationDao.insertAll(any()) } just runs

        runBlocking { underTest.storeContactGroups(contactId, contactGroupEntities) }

        coVerify { contactGroupDao.getGroupNames() }
        coVerify { contactGroupDao.insertAll(newContactGroupEntities) }
    }

    @Test
    fun `should replace the existing group-relations with the new ones`() {
        val contactId = someContactId()
        val existingContactGroupsEntities = listOf(
            someContactGroupEntity(name = "Group 1", notes = "Notes 1"),
            someContactGroupEntity(name = "Group 2", notes = "Notes 2"),
            someContactGroupEntity(name = "Group 3", notes = "Notes 3"),
        )
        val newContactGroupEntities = listOf(
            someContactGroupEntity(name = "New Group 1"),
            someContactGroupEntity(name = "New Group 2"),
        )
        val existingContactGroupNames = existingContactGroupsEntities.map { it.name }
        val contactGroupEntities = existingContactGroupsEntities.take(2) + newContactGroupEntities
        val contactGroupRelations = contactGroupEntities.map {
            someContactGroupRelationEntity(contactId = contactId.uuid, groupName = it.name)
        }
        coEvery { contactGroupDao.getGroupNames() } returns existingContactGroupNames
        coEvery { contactGroupDao.insertAll(any()) } just runs
        coEvery { contactGroupRelationDao.deleteRelationsForContact(any()) } just runs
        coEvery { contactGroupRelationDao.insertAll(any()) } just runs

        runBlocking { underTest.storeContactGroups(contactId, contactGroupEntities) }

        coVerify { contactGroupRelationDao.deleteRelationsForContact(contactId.uuid) }
        coVerify { contactGroupRelationDao.insertAll(contactGroupRelations) }
    }
}
