/*
 * Private Contacts
 * Copyright (c) 2026.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.routing

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import ch.abwesend.privatecontacts.domain.lib.logging.error
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.contact.ContactId

/**
 * Owns the navigation back-stack of the app.
 *
 * Being a view-model scoped to the activity, the back-stack survives configuration-changes
 * but is intentionally lost on process-death: the app should start fresh on the contact-list.
 * This also allows routes to carry objects which are not serializable (see [ContactEditTarget.Unsaved]).
 */
class NavigationViewModel : ViewModel() {
    private var instanceCounter: Int = 0

    val backStack: SnapshotStateList<ScreenRoute> =
        mutableStateListOf(SelfInitializingRoute(Screen.ContactList, nextInstanceId()))

    /**
     * The root of the back-stack is always the contact-list: navigating to it returns to that root-entry
     * instead of adding a new one (which would come with a new view-model and therefore lose the
     * selected tab, the search, the scroll-position etc.).
     */
    fun navigateTo(screen: Screen): Boolean {
        if (!screen.selfInitializing) {
            logger.error("Trying to navigate to screen '$screen' without passing data")
        } else if (screen == Screen.ContactList) {
            returnToRoot()
        } else {
            backStack.add(SelfInitializingRoute(screen, nextInstanceId()))
        }
        return screen.selfInitializing
    }

    private fun returnToRoot() {
        while (backStack.size > 1) {
            backStack.removeAt(backStack.lastIndex)
        }
    }

    fun navigateToContactDetail(contactId: ContactId) {
        backStack.add(ContactDetailRoute(contactId, nextInstanceId()))
    }

    fun navigateToContactEdit(target: ContactEditTarget) {
        backStack.add(ContactEditRoute(target, nextInstanceId()))
    }

    fun navigateUp(): Boolean {
        val canNavigateUp = backStack.size > 1
        if (canNavigateUp) {
            backStack.removeAt(backStack.lastIndex)
        }
        return canNavigateUp
    }

    /**
     * Leaves the current screen. If the screen below is a contact-detail screen, it is replaced by a
     * new instance (showing [contactId] if passed): the contact may have changed, including its ID.
     */
    fun returnToContactDetail(contactId: ContactId?): Boolean {
        val navigatedUp = navigateUp()
        val currentRoute = backStack.last()
        if (navigatedUp && currentRoute is ContactDetailRoute) {
            val newContactId = contactId ?: currentRoute.contactId
            backStack[backStack.lastIndex] = ContactDetailRoute(newContactId, nextInstanceId())
        }
        return navigatedUp
    }

    /**
     * Rebuilds the current screen from scratch, e.g. after the app-language was changed:
     * replacing the top-most entry by a new instance of the same route discards its state
     * without touching the entries below it.
     */
    fun refreshCurrentScreen() {
        val currentRoute = backStack.lastOrNull()
        if (currentRoute != null) {
            backStack[backStack.lastIndex] = currentRoute.withInstanceId(nextInstanceId())
        }
    }

    private fun nextInstanceId(): Int {
        instanceCounter += 1
        return instanceCounter
    }
}
