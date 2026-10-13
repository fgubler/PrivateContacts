/*
 * Private Contacts
 * Copyright (c) 2026.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.routing

import ch.abwesend.privatecontacts.domain.model.contact.ContactId
import ch.abwesend.privatecontacts.testutil.TestBase
import ch.abwesend.privatecontacts.testutil.databuilders.someContactId
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
    fun `navigateTo should refuse screens which need data`() {
        val rootEntry = underTest.backStack.single()

        val result = underTest.navigateTo(Screen.ContactDetail)

        assertThat(result).isFalse
        assertThat(underTest.backStack).containsExactly(rootEntry)
    }

    @Test
    fun `navigateToContactDetail should pass the contact-id via the route`() {
        val contactId: ContactId = someContactId()

        underTest.navigateToContactDetail(contactId)

        val route = underTest.backStack.last()
        assertThat(route).isInstanceOf(ContactDetailRoute::class.java)
        assertThat((route as ContactDetailRoute).contactId).isEqualTo(contactId)
        assertThat(route.screen).isEqualTo(Screen.ContactDetail)
    }

    @Test
    fun `navigateToContactEdit should pass the target via the route`() {
        val target = ContactEditTarget.Existing(someContactId())

        underTest.navigateToContactEdit(target)

        val route = underTest.backStack.last()
        assertThat(route).isInstanceOf(ContactEditRoute::class.java)
        assertThat((route as ContactEditRoute).target).isEqualTo(target)
        assertThat(route.screen).isEqualTo(Screen.ContactEdit)
    }

    @Test
    fun `returnToContactDetail should replace the detail-screen by a new instance`() {
        val contactId: ContactId = someContactId()
        underTest.navigateToContactDetail(contactId)
        val detailRouteBefore = underTest.backStack.last()
        underTest.navigateToContactEdit(ContactEditTarget.Existing(contactId))

        val result = underTest.returnToContactDetail(contactId)

        assertThat(result).isTrue
        assertThat(underTest.backStack).hasSize(2)
        val detailRouteAfter = underTest.backStack.last() as ContactDetailRoute
        assertThat(detailRouteAfter.contactId).isEqualTo(contactId)
        assertThat(detailRouteAfter.contentKey).isNotEqualTo(detailRouteBefore.contentKey)
    }

    @Test
    fun `returnToContactDetail should show the new contact-id`() {
        val oldContactId: ContactId = someContactId()
        val newContactId: ContactId = someContactId()
        underTest.navigateToContactDetail(oldContactId)
        underTest.navigateToContactEdit(ContactEditTarget.Existing(oldContactId))

        underTest.returnToContactDetail(newContactId)

        val detailRoute = underTest.backStack.last() as ContactDetailRoute
        assertThat(detailRoute.contactId).isEqualTo(newContactId)
    }

    @Test
    fun `returnToContactDetail should keep the contact-id if none is passed`() {
        val contactId: ContactId = someContactId()
        underTest.navigateToContactDetail(contactId)
        underTest.navigateToContactEdit(ContactEditTarget.Existing(contactId))

        underTest.returnToContactDetail(contactId = null)

        val detailRoute = underTest.backStack.last() as ContactDetailRoute
        assertThat(detailRoute.contactId).isEqualTo(contactId)
    }

    @Test
    fun `returnToContactDetail should just navigate up if there is no detail-screen below`() {
        val rootEntry = underTest.backStack.single()
        underTest.navigateToContactEdit(ContactEditTarget.New)

        val result = underTest.returnToContactDetail(someContactId())

        assertThat(result).isTrue
        assertThat(underTest.backStack).containsExactly(rootEntry)
    }

    @Test
    fun `refreshCurrentScreen should keep the data of the route`() {
        val contactId: ContactId = someContactId()
        underTest.navigateToContactDetail(contactId)
        val routeBefore = underTest.backStack.last()

        underTest.refreshCurrentScreen()

        val routeAfter = underTest.backStack.last() as ContactDetailRoute
        assertThat(routeAfter.contactId).isEqualTo(contactId)
        assertThat(routeAfter.contentKey).isNotEqualTo(routeBefore.contentKey)
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
