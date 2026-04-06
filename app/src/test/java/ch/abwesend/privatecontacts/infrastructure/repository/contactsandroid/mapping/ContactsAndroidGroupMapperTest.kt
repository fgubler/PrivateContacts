/*
 * Private Contacts
 * Copyright (c) 2026.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository.contactsandroid.mapping

import ch.abwesend.privatecontacts.domain.model.ModelStatus
import ch.abwesend.privatecontacts.testutil.databuilders.someContactsAndroidGroup
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ContactsAndroidGroupMapperTest {

    @Test
    fun `should map group to ContactGroup`() {
        val group = someContactsAndroidGroup(id = 42L, title = "Friends")

        val result = group.toContactGroup()

        assertThat(result.id.name).isEqualTo("Friends")
        assertThat(result.id.groupNo).isEqualTo(42L)
        assertThat(result.notes).isEmpty()
        assertThat(result.modelStatus).isEqualTo(ModelStatus.UNCHANGED)
    }

    @Test
    fun `should map list of groups to ContactGroups`() {
        val groups = listOf(
            someContactsAndroidGroup(id = 1L, title = "Friends"),
            someContactsAndroidGroup(id = 2L, title = "Family"),
            someContactsAndroidGroup(id = 3L, title = "Work"),
        )

        val result = groups.toContactGroups()

        assertThat(result).hasSize(3)
        assertThat(result.map { it.id.name }).containsExactly("Friends", "Family", "Work")
    }

    @Test
    fun `should deduplicate groups by name`() {
        val groups = listOf(
            someContactsAndroidGroup(id = 1L, title = "Friends"),
            someContactsAndroidGroup(id = 2L, title = "Friends"),
            someContactsAndroidGroup(id = 3L, title = "Work"),
        )

        val result = groups.toContactGroups()

        assertThat(result).hasSize(2)
        assertThat(result.map { it.id.name }).containsExactly("Friends", "Work")
    }

    @Test
    fun `should return empty list for empty input`() {
        val result = emptyList<contacts.core.entities.Group>().toContactGroups()

        assertThat(result).isEmpty()
    }
}
