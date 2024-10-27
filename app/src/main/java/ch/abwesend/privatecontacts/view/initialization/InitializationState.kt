/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.initialization

sealed interface InitializationState {
    fun next(): InitializationState

    sealed interface InfoDialogState : InitializationState

    data object InitialInfoDialog : InfoDialogState {
        override fun next(): InitializationState = NewFeaturesDialog
    }

    data object NewFeaturesDialog : InfoDialogState {
        override fun next(): InitializationState = ReviewDialog
    }

    data object ReviewDialog : InfoDialogState {
        override fun next(): InitializationState = CallPermissionsDialog
    }

    data object CallPermissionsDialog : InitializationState {
        override fun next(): InitializationState = Initialized
    }

    data object Initialized : InitializationState {
        override fun next(): InitializationState = this
    }
}
