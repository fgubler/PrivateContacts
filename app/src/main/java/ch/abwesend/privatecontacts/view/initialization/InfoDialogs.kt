/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.initialization

import android.app.Activity
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.lib.logging.logger
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
import com.google.android.play.core.review.ReviewException
import com.google.android.play.core.review.ReviewManagerFactory
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
    if (!shouldShowDialog(settings) || activity == null) {
        close()
        return
    }

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
                showAndroidReview(activity, close)
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
        settings.numberOfAppStarts.mod(31) == 0 &&
        daysSinceLastUserPrompt > 35 // should not prompt more than once a month
}

// TODO could be rewritten to use suspend-functions
private fun showAndroidReview(activity: Activity, close: () -> Unit) {
    val reviewManager = ReviewManagerFactory.create(activity)
    reviewManager.requestReviewFlow().addOnCompleteListener { preparationTask ->
        if (preparationTask.isSuccessful && preparationTask.result != null) {
            val reviewInfoResult = preparationTask.result
            reviewManager.launchReviewFlow(activity, reviewInfoResult).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Settings.repository.showReviewDialog = false // already shown
                    Toast.makeText(activity, R.string.thank_you, Toast.LENGTH_SHORT).show()
                }
                close()
            }
        } else {
            val errorCode = (preparationTask.exception as? ReviewException)?.errorCode?.toString() ?: "[NO_ERROR_CODE]"
            activity.logger.info("Failed request review flow: errorCode = '$errorCode'")
            Toast.makeText(activity, R.string.unexpected_error_apology, Toast.LENGTH_SHORT).show()
            close()
        }
    }
}
