/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.initialization

sealed interface InitializationState {
    val showMainContent: Boolean
    fun next(): InitializationState

    object WaitingForSettings : InitializationState {
        override val showMainContent: Boolean = false
        override fun next(): InitializationState = InitialInfoDialog
    }

    object InitialInfoDialog : InitializationState {
        override val showMainContent: Boolean = true
        override fun next(): InitializationState = NewFeaturesDialog
    }

    object NewFeaturesDialog : InitializationState {
        override val showMainContent: Boolean = true
        override fun next(): InitializationState = CallPermissionsDialog
    }

    object CallPermissionsDialog : InitializationState {
        override val showMainContent: Boolean = true
        override fun next(): InitializationState = Initialized
    }

    object Initialized : InitializationState {
        override val showMainContent: Boolean = true
        override fun next(): InitializationState = this
    }
}
