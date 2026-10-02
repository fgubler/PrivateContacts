/*
 * Private Contacts
 * Copyright (c) 2026.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.routing

import ch.abwesend.privatecontacts.testutil.TestBase
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockKExtension::class)
class NavigationViewModelTest : TestBase() {
    private val underTest = NavigationViewModel()

    @Test
    fun `should start on the contact-list`() {
        assertThat(underTest.backStack).hasSize(1)
        assertThat(underTest.backStack.single().screen).isEqualTo(Screen.ContactList)
    }

    @Test
    fun `should give entries of the same screen different content-keys`() {
        underTest.navigateTo(Screen.Settings)
        underTest.navigateTo(Screen.ContactList)

        val contentKeys = underTest.backStack.map { it.contentKey }

        assertThat(underTest.backStack.map { it.screen })
            .containsExactly(Screen.ContactList, Screen.Settings, Screen.ContactList)
        assertThat(contentKeys).doesNotHaveDuplicates()
    }

    @Test
    fun `navigateUp should remove the top-most entry`() {
        underTest.navigateTo(Screen.Settings)
        val rootEntry = underTest.backStack.first()

        val result = underTest.navigateUp()

        assertThat(result).isTrue
        assertThat(underTest.backStack).containsExactly(rootEntry)
    }

    @Test
    fun `navigateUp should do nothing on the root-screen`() {
        val rootEntry = underTest.backStack.single()

        val result = underTest.navigateUp()

        assertThat(result).isFalse
        assertThat(underTest.backStack).containsExactly(rootEntry)
    }

    @Test
    fun `refreshCurrentScreen should replace the top-most entry by a new instance`() {
        underTest.navigateTo(Screen.Settings)
        val entriesBefore = underTest.backStack.toList()

        underTest.refreshCurrentScreen()

        val entriesAfter = underTest.backStack.toList()
        assertThat(entriesAfter).hasSameSizeAs(entriesBefore)
        assertThat(entriesAfter.last().screen).isEqualTo(Screen.Settings)
        assertThat(entriesAfter.last()).isNotEqualTo(entriesBefore.last())
        assertThat(entriesAfter.last().contentKey).isNotEqualTo(entriesBefore.last().contentKey)
    }

    @Test
    fun `refreshCurrentScreen should leave the entries below the current one untouched`() {
        underTest.navigateTo(Screen.Settings)
        val rootEntryBefore = underTest.backStack.first()

        underTest.refreshCurrentScreen()

        assertThat(underTest.backStack.first()).isEqualTo(rootEntryBefore)
        assertThat(underTest.backStack.first().contentKey).isEqualTo(rootEntryBefore.contentKey)
    }
}
