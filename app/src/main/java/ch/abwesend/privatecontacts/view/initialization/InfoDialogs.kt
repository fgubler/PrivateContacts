/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.initialization

import androidx.compose.runtime.Composable
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.settings.ISettingsState
import ch.abwesend.privatecontacts.domain.settings.Settings
import ch.abwesend.privatecontacts.view.components.dialogs.OkDoNotShowAgainDialog
import ch.abwesend.privatecontacts.view.initialization.InitializationState.InitialInfoDialog

@Composable
fun InfoDialogs(
    initializationState: InitializationState,
    settings: ISettingsState,
    goToNextState: () -> Unit
) {
    when (initializationState) {
        InitialInfoDialog -> InitialAppInfoDialog(settings, goToNextState)
        InitializationState.NewFeaturesDialog -> goToNextState() // TODO implement
        else -> Unit
    }
}

@Composable
private fun InitialAppInfoDialog(settings: ISettingsState, close: () -> Unit) {
    if (!settings.showInitialAppInfoDialog) {
        close()
        return
    }

    OkDoNotShowAgainDialog(
        title = R.string.app_name,
        text = R.string.app_introduction_description,
    ) { doNotShowAgain ->
        close()
        if (doNotShowAgain) {
            Settings.repository.showInitialAppInfoDialog = false
        }
    }
}
