/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.initialization

import androidx.compose.runtime.Composable
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.Settings
import ch.abwesend.privatecontacts.view.components.dialogs.OkDoNotShowAgainDialog
import ch.abwesend.privatecontacts.view.initialization.InitializationState.InitialInfoDialog

@Composable
fun InfoDialogs(initializationState: InitializationState, goToNextState: () -> Unit) {
    when (initializationState) {
        InitialInfoDialog -> InitialAppInfoDialog(goToNextState)
        InitializationState.NewFeaturesDialog -> goToNextState() // TODO implement
        else -> Unit
    }
}

@Composable
private fun InitialAppInfoDialog(close: () -> Unit) {
    if (!Settings.showInitialAppInfoDialog) {
        return
    }

    OkDoNotShowAgainDialog(
        title = R.string.app_name,
        text = R.string.app_introduction_description,
    ) {
        close()
        Settings.showInitialAppInfoDialog = false
    }
}
