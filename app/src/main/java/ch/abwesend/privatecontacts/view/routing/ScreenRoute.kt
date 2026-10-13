/*
 * Private Contacts
 * Copyright (c) 2026.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.routing

import androidx.navigation3.runtime.NavKey
import ch.abwesend.privatecontacts.domain.model.contact.ContactId
import ch.abwesend.privatecontacts.domain.model.contact.IContact

/**
 * One entry of the navigation back-stack: the [screen] to be shown, the data it needs and an
 * [instanceId] which distinguishes this entry from other entries showing the same [screen].
 *
 * Navigation3 identifies the content of an entry by its content-key: two entries with the same
 * key would share their saved state and their movable content, so each entry needs its own.
 *
 * The back-stack is never serialized (see [NavigationViewModel]), so routes may carry
 * in-memory objects which are not [java.io.Serializable].
 */
sealed interface ScreenRoute : NavKey {
    val screen: Screen
    val instanceId: Int

    val contentKey: String
        get() = "${screen.key}-$instanceId"

    fun withInstanceId(instanceId: Int): ScreenRoute
}

/** a screen which does not need any data from the previous screen: see [Screen.selfInitializing] */
data class SelfInitializingRoute(override val screen: Screen, override val instanceId: Int) : ScreenRoute {
    override fun withInstanceId(instanceId: Int): ScreenRoute = copy(instanceId = instanceId)
}

data class ContactDetailRoute(val contactId: ContactId, override val instanceId: Int) : ScreenRoute {
    override val screen: Screen = Screen.ContactDetail

    override fun withInstanceId(instanceId: Int): ScreenRoute = copy(instanceId = instanceId)
}

data class ContactEditRoute(val target: ContactEditTarget, override val instanceId: Int) : ScreenRoute {
    override val screen: Screen = Screen.ContactEdit

    override fun withInstanceId(instanceId: Int): ScreenRoute = copy(instanceId = instanceId)
}

sealed interface ContactEditTarget {
    data object New : ContactEditTarget
    data class Existing(val contactId: ContactId) : ContactEditTarget

    /** a contact which is not persisted (yet), e.g. parsed from a vcf-file */
    data class Unsaved(val contact: IContact) : ContactEditTarget
}
