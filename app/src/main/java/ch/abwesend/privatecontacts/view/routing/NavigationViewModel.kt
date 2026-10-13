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
 * but is intentionally lost on process-death: the screens get their data from view-models
 * which are lost as well, so restoring the back-stack would show screens without their data.
 */
class NavigationViewModel : ViewModel() {
    private var instanceCounter: Int = 0

    val backStack: SnapshotStateList<ScreenRoute> =
        mutableStateListOf(SelfInitializingRoute(Screen.ContactList, nextInstanceId()))

    fun navigateTo(screen: Screen): Boolean {
        if (screen.selfInitializing) {
            backStack.add(SelfInitializingRoute(screen, nextInstanceId()))
        } else {
            logger.error("Trying to navigate to screen '$screen' without passing data")
        }
        return screen.selfInitializing
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
