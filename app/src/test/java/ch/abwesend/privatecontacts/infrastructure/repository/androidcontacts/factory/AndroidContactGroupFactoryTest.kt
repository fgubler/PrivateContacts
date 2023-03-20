/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.factory

import ch.abwesend.privatecontacts.domain.model.ModelStatus
import ch.abwesend.privatecontacts.testutil.TestBase
import ch.abwesend.privatecontacts.testutil.databuilders.someAndroidContactGroup
import ch.abwesend.privatecontacts.testutil.databuilders.someContactGroup
import ch.abwesend.privatecontacts.testutil.databuilders.someInternetAccount
import ch.abwesend.privatecontacts.testutil.databuilders.someOnlineAccount
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExperimentalCoroutinesApi
@ExtendWith(MockKExtension::class)
class AndroidContactGroupFactoryTest : TestBase() {
    @Test
    fun `should convert single AndroidContactGroup to internal ContactGroup`() {
        val androidContactGroup = someAndroidContactGroup(title = "group1", notes = "note1", groupId = 123)

        val result = androidContactGroup.toContactGroup()

        assertThat(result.id.groupNo).isEqualTo(androidContactGroup.groupId)
        assertThat(result.id.name).isEqualTo(androidContactGroup.title)
        assertThat(result.notes).isEqualTo(androidContactGroup.note)
        assertThat(result.modelStatus).isEqualTo(ModelStatus.UNCHANGED)
    }

    @Test
    fun `should convert multiple AndroidContactGroups to internal ContactGroups`() {
        val androidContactGroups = listOf(
            someAndroidContactGroup(title = "group1", notes = "note1", groupId = 123),
            someAndroidContactGroup(title = "group2", notes = "note2", groupId = 234),
            someAndroidContactGroup(title = "group3", notes = "note3", groupId = 345),
        )

        val result = androidContactGroups.toContactGroups()

        assertThat(result).hasSameSizeAs(androidContactGroups)
        androidContactGroups.indices.forEach { index ->
            assertThat(result[index].id.groupNo).isEqualTo(androidContactGroups[index].groupId)
            assertThat(result[index].id.name).isEqualTo(androidContactGroups[index].title)
            assertThat(result[index].notes).isEqualTo(androidContactGroups[index].note)
            assertThat(result[index].modelStatus).isEqualTo(ModelStatus.UNCHANGED)
        }
    }

    @Test
    fun `should filter out name-duplicates`() {
        val androidContactGroups = listOf(
            someAndroidContactGroup(title = "group", notes = "note1", groupId = 123),
            someAndroidContactGroup(title = "group", notes = "note2", groupId = 234),
            someAndroidContactGroup(title = "group", notes = "note3", groupId = 345),
        )

        val results = androidContactGroups.toContactGroups()

        assertThat(results).hasSize(1)
        val result = results.first()
        assertThat(result.id.groupNo).isEqualTo(androidContactGroups.first().groupId)
        assertThat(result.id.name).isEqualTo(androidContactGroups.first().title)
        assertThat(result.notes).isEqualTo(androidContactGroups.first().note)
    }

    @Test
    fun `should convert single internal ContactGroup to AndroidContactGroup`() {
        val contactGroup = someContactGroup(name = "group", notes = "notes")
        val account = someOnlineAccount()
        val expectedInternetAccount = someInternetAccount(name = account.username, type = account.accountProvider)

        val result = contactGroup.toNewAndroidContactGroup(account)

        assertThat(result.groupId).isEqualTo(-1L)
        assertThat(result.title).isEqualTo(contactGroup.id.name)
        assertThat(result.note).isEqualTo(contactGroup.notes)
        assertThat(result.account).isNotNull.isEqualTo(expectedInternetAccount)
    }
}
