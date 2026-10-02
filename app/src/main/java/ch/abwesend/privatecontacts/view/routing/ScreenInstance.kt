/*
 * Private Contacts
 * Copyright (c) 2026.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.routing

import androidx.navigation3.runtime.NavKey

/**
 * One entry of the navigation back-stack: the [screen] to be shown plus an [instanceId] which
 * distinguishes this entry from other entries showing the same [screen].
 *
 * Navigation3 identifies the content of an entry by its content-key: two entries with the same
 * key would share their saved state and their movable content, so each entry needs its own.
 */
data class ScreenInstance(val screen: Screen, val instanceId: Int) : NavKey {
    val contentKey: String = "${screen.key}-$instanceId"
}
