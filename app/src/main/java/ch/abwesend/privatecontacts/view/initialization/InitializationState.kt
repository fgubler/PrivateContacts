/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.initialization

import ch.abwesend.privatecontacts.domain.model.backup.BackupMessage

sealed interface InitializationState {
    suspend fun next(): InitializationState

    sealed interface InfoDialogState : InitializationState

    data class InitialState(val initialize: suspend () -> InitializationState) : InitializationState {
        override suspend fun next(): InitializationState = initialize()
    }

    data class BackupMessagesDialog(val messages: List<BackupMessage>) : InitializationState {
        override suspend fun next(): InitializationState = InitialInfoDialog
    }

    data object InitialInfoDialog : InfoDialogState {
        override suspend fun next(): InitializationState = NewFeaturesDialog
    }

    data object NewFeaturesDialog : InfoDialogState {
        override suspend fun next(): InitializationState = ReviewDialog
    }

    data object ReviewDialog : InfoDialogState {
        override suspend fun next(): InitializationState = CallPermissionsDialog
    }

    data object CallPermissionsDialog : InitializationState {
        override suspend fun next(): InitializationState = Initialized
    }

    data object Initialized : InitializationState {
        override suspend fun next(): InitializationState = this
    }
}
