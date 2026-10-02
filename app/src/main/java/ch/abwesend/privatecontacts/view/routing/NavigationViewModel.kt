/*
 * Private Contacts
 * Copyright (c) 2026.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.routing

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel

/**
 * Owns the navigation back-stack of the app.
 *
 * Being a view-model scoped to the activity, the back-stack survives configuration-changes
 * but is intentionally lost on process-death: the screens get their data from view-models
 * which are lost as well, so restoring the back-stack would show screens without their data.
 */
class NavigationViewModel : ViewModel() {
    val backStack: SnapshotStateList<Screen> = mutableStateListOf(Screen.ContactList)

    /**
     * Changing this value rebuilds the content of the current screen
     * (see the content-key of the nav-entries in MainNavHost).
     */
    var refreshCounter: Int by mutableIntStateOf(0)
        private set

    fun navigateTo(screen: Screen) {
        backStack.add(screen)
    }

    fun navigateUp(): Boolean {
        val canNavigateUp = backStack.size > 1
        if (canNavigateUp) {
            backStack.removeAt(backStack.lastIndex)
        }
        return canNavigateUp
    }

    /** rebuilds the current screen from scratch, e.g. after the app-language was changed */
    fun refreshCurrentScreen() {
        refreshCounter += 1
    }
}
