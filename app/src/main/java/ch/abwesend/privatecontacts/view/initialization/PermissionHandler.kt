/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.initialization

import android.Manifest.permission.READ_PHONE_STATE
import android.os.Build
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
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
import ch.abwesend.privatecontacts.view.permission.PermissionRequestResult.ALREADY_GRANTED

@Composable
fun ComponentActivity.PermissionHandler(
    settings: ISettingsState,
    permissionHelper: PermissionHelper,
    onPermissionsHandled: () -> Unit,
) {
    var requestIncomingCallPermissions by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!permissionHelper.callIdentificationPossible) {
            Settings.repository.observeIncomingCalls = false
            Settings.repository.requestIncomingCallPermissions = false
            onPermissionsHandled()
            return@LaunchedEffect
        }

        requestPermissionsForCallerIdentification(
            settings = settings,
            permissionHelper = permissionHelper,
            showExplanation = { requestIncomingCallPermissions = true }
        ) {
            if (it == ALREADY_GRANTED) {
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
                requestPermissionsForCallerIdentification(
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

private fun ComponentActivity.requestPermissionsForCallerIdentification(
    settings: ISettingsState,
    permissionHelper: PermissionHelper,
    showExplanation: (() -> Unit)?,
    onResult: ((PermissionRequestResult) -> Unit)?,
) {
    if (!settings.observeIncomingCalls || !settings.requestIncomingCallPermissions) {
        return
    }

    val onPermissionResult: (PermissionRequestResult) -> Unit = { result ->
        if (result == PermissionRequestResult.NEWLY_GRANTED) {
            Settings.repository.observeIncomingCalls = true
            Toast.makeText(this, R.string.thank_you, Toast.LENGTH_SHORT).show()
        }
        onResult?.invoke(result)
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        requestCallScreeningServiceRole(
            permissionHelper = permissionHelper,
            showExplanation = showExplanation,
            onPermissionResult = onPermissionResult,
        )
    } else {
        requestPhoneStatePermission(
            permissionHelper = permissionHelper,
            showExplanation = showExplanation,
            onPermissionResult = onPermissionResult,
        )
    }
}

@RequiresApi(Build.VERSION_CODES.Q)
private fun ComponentActivity.requestCallScreeningServiceRole(
    permissionHelper: PermissionHelper,
    showExplanation: (() -> Unit)?,
    onPermissionResult: (PermissionRequestResult) -> Unit,
) {
    showExplanation?.let {
        permissionHelper.requestCallerIdRoleWithExplanation(
            activity = this,
            showExplanation = it,
            onPermissionResult = onPermissionResult
        )
    } ?: permissionHelper.requestCallerIdRoleNow(
        activity = this,
        onPermissionResult = onPermissionResult,
    )
}

private fun ComponentActivity.requestPhoneStatePermission(
    permissionHelper: PermissionHelper,
    showExplanation: (() -> Unit)?,
    onPermissionResult: (PermissionRequestResult) -> Unit,
) {
    val permissions = listOf(READ_PHONE_STATE)

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
