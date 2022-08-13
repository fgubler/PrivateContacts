/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.room.contact

import ch.abwesend.privatecontacts.infrastructure.room.contactgroup.toContactGroup
import ch.abwesend.privatecontacts.infrastructure.room.contactgroup.toEntity
import ch.abwesend.privatecontacts.testutil.TestBase
import ch.abwesend.privatecontacts.testutil.databuilders.someContactGroup
import ch.abwesend.privatecontacts.testutil.databuilders.someContactGroupEntity
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExperimentalCoroutinesApi
@ExtendWith(MockKExtension::class)
class ContactGroupEntityExtensionsTest : TestBase() {
    @Test
    fun `should copy name and notes to entity`() {
        val contactGroup = someContactGroup(name = "GroupName", notes = "GroupNotes")

        val result = contactGroup.toEntity()

        assertThat(result.name).isEqualTo(contactGroup.id.name)
        assertThat(result.notes).isEqualTo(contactGroup.notes)
    }

    @Test
    fun `should copy name and notes from entity`() {
        val entity = someContactGroupEntity(name = "GroupName", notes = "GroupNotes")

        val result = entity.toContactGroup()

        assertThat(result.id.name).isEqualTo(entity.name)
        assertThat(result.notes).isEqualTo(entity.notes)
    }
}
