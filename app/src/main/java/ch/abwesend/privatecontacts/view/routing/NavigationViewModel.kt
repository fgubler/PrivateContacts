/*
 * Private Contacts
 * Copyright (c) 2026.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.routing

import androidx.compose.runtime.mutableStateListOf
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
    private var instanceCounter: Int = 0

    val backStack: SnapshotStateList<ScreenInstance> =
        mutableStateListOf(createInstance(Screen.ContactList))

    fun navigateTo(screen: Screen) {
        backStack.add(createInstance(screen))
    }

    fun navigateUp(): Boolean {
        val canNavigateUp = backStack.size > 1
        if (canNavigateUp) {
            backStack.removeAt(backStack.lastIndex)
        }
        return canNavigateUp
    }

    /**
     * Rebuilds the current screen from scratch, e.g. after the app-language was changed:
     * replacing the top-most entry by a new instance of the same screen discards its state
     * without touching the entries below it.
     */
    fun refreshCurrentScreen() {
        val currentScreen = backStack.lastOrNull()?.screen
        if (currentScreen != null) {
            backStack[backStack.lastIndex] = createInstance(currentScreen)
        }
    }

    private fun createInstance(screen: Screen): ScreenInstance {
        instanceCounter += 1
        return ScreenInstance(screen = screen, instanceId = instanceCounter)
    }
}
