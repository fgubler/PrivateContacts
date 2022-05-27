/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.initialization

sealed interface InitializationState {
    fun next(): InitializationState

    object InitialInfoDialog : InitializationState {
        override fun next(): InitializationState = NewFeaturesDialog
    }

    object NewFeaturesDialog : InitializationState {
        override fun next(): InitializationState = AndroidContactPermissionsDialog
    }

    object AndroidContactPermissionsDialog : InitializationState {
        override fun next(): InitializationState = CallPermissionsDialog
    }

    object CallPermissionsDialog : InitializationState {
        override fun next(): InitializationState = Initialized
    }

    object Initialized : InitializationState {
        override fun next(): InitializationState = this
    }
}
