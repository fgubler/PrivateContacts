/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.initialization

import android.Manifest
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.settings.ISettingsState
import ch.abwesend.privatecontacts.domain.settings.Settings
import ch.abwesend.privatecontacts.view.components.dialogs.YesNoNeverDialog
import ch.abwesend.privatecontacts.view.permission.PermissionHelper
import ch.abwesend.privatecontacts.view.permission.PermissionRequestResult

@Composable
fun ComponentActivity.PermissionHandler(
    settings: ISettingsState,
    permissionHelper: PermissionHelper,
    onPermissionsHandled: () -> Unit,
) {
    var requestIncomingCallPermissions by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        requestPhoneStatePermission(
            settings = settings,
            permissionHelper = permissionHelper,
            showExplanation = { requestIncomingCallPermissions = true }
        ) {
            if (it == PermissionRequestResult.ALREADY_GRANTED) {
                onPermissionsHandled()
            }
        }
    }

    IncomingCallPermissionDialog(
        settings = settings,
        permissionHelper = permissionHelper,
        showDialog = requestIncomingCallPermissions
    ) {
        requestIncomingCallPermissions = false
        onPermissionsHandled()
    }
}

@Composable
private fun ComponentActivity.IncomingCallPermissionDialog(
    settings: ISettingsState,
    permissionHelper: PermissionHelper,
    showDialog: Boolean,
    closeDialog: () -> Unit,
) {
    if (showDialog) {
        YesNoNeverDialog(
            title = R.string.show_caller_information_title,
            text = R.string.show_caller_information_text,
            secondaryTextBlock = R.string.activate_feature,
            onYes = {
                closeDialog()
                requestPhoneStatePermission(
                    settings = settings,
                    permissionHelper = permissionHelper,
                    showExplanation = null,
                    onResult = null,
                )
            },
            onNo = { doNotShowAgain ->
                closeDialog()
                Settings.repository.observeIncomingCalls = false
                if (doNotShowAgain) {
                    Settings.repository.requestIncomingCallPermissions = false
                }
            }
        )
    }
}

private fun ComponentActivity.requestPhoneStatePermission(
    settings: ISettingsState,
    permissionHelper: PermissionHelper,
    showExplanation: (() -> Unit)?,
    onResult: ((PermissionRequestResult) -> Unit)?,
) {
    if (!settings.requestIncomingCallPermissions) {
        return
    }

    val onPermissionResult: (PermissionRequestResult) -> Unit = { result ->
        if (result == PermissionRequestResult.NEWLY_GRANTED) {
            Settings.repository.observeIncomingCalls = true
            Toast.makeText(this, R.string.thank_you, Toast.LENGTH_SHORT).show()
        }
        onResult?.invoke(result)
    }

    val permissions = listOf(
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.READ_CALL_LOG,
    )

    showExplanation?.let {
        permissionHelper.requestUserPermissionsWithExplanation(
            activity = this,
            permissions = permissions,
            onShowExplanation = showExplanation,
            onPermissionResult = onPermissionResult
        )
    } ?: permissionHelper.requestUserPermissionsNow(
        permissions = permissions,
        onPermissionResult = onPermissionResult
    )
}
