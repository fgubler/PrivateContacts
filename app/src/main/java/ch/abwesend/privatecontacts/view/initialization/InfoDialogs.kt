/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.initialization

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.settings.ISettingsState
import ch.abwesend.privatecontacts.domain.settings.Settings
import ch.abwesend.privatecontacts.view.components.dialogs.OkDoNotShowAgainDialog
import ch.abwesend.privatecontacts.view.components.dialogs.SimpleProgressDialog
import ch.abwesend.privatecontacts.view.components.dialogs.YesNoNeverDialog
import ch.abwesend.privatecontacts.view.initialization.InitializationState.InfoDialogState
import ch.abwesend.privatecontacts.view.initialization.InitializationState.InitialInfoDialog
import ch.abwesend.privatecontacts.view.initialization.InitializationState.NewFeaturesDialog
import ch.abwesend.privatecontacts.view.initialization.InitializationState.ReviewDialog
import ch.abwesend.privatecontacts.view.util.getCurrentActivity
import ch.abwesend.privatecontacts.view.util.showAndroidReview
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.Period

@Composable
fun InfoDialogs(
    initializationState: InfoDialogState,
    settings: ISettingsState,
    goToNextState: () -> Unit
) {
    when (initializationState) {
        InitialInfoDialog -> InitialAppInfoDialog(settings, goToNextState)
        ReviewDialog -> ReviewDialog(settings, goToNextState)
        NewFeaturesDialog -> goToNextState() // TODO implement
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

@Composable
private fun ReviewDialog(settings: ISettingsState, close: () -> Unit) {
    val activity = getCurrentActivity()
    if (activity == null || !shouldShowDialog(settings)) {
        close()
        return
    }

    val coroutineScope = rememberCoroutineScope()
    var showSpinner: Boolean by remember { mutableStateOf(false) }

    if (showSpinner) {
        SimpleProgressDialog(
            title = R.string.review_dialog_title,
            allowRunningInBackground = false
        )
    } else {
        YesNoNeverDialog(
            title = R.string.review_dialog_title,
            text = R.string.review_dialog_text,
            secondaryTextBlock = R.string.review_dialog_secondary_text,
            onYes = {
                showSpinner = true
                showAndroidReview(activity, coroutineScope, close)
            },
            onNo = { doNotShowAgain ->
                close()
                if (doNotShowAgain) {
                    Settings.repository.showReviewDialog = false
                }
            }
        )
    }
}

private fun shouldShowDialog(settings: ISettingsState): Boolean {
    val daysSinceLastUserPrompt = Period.between(LocalDate.now(), settings.latestUserPromptAtStartup).days
    return settings.showReviewDialog &&
        settings.numberOfAppStarts.mod(23) == 0 && // ask on every n-th startup
        daysSinceLastUserPrompt > 35 // should not prompt more than once a month
}

private fun showAndroidReview(activity: Activity, coroutineScope: CoroutineScope, close: () -> Unit) {
    coroutineScope.launch {
        activity.showAndroidReview()
        close()
    }
}
