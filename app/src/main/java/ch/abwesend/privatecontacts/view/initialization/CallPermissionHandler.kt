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
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.settings.ISettingsState
import ch.abwesend.privatecontacts.domain.settings.Settings
import ch.abwesend.privatecontacts.domain.util.callIdentificationPossible
import ch.abwesend.privatecontacts.domain.util.canUseCallScreeningService
import ch.abwesend.privatecontacts.view.components.dialogs.YesNoNeverDialog
import ch.abwesend.privatecontacts.view.permission.PermissionHelper
import ch.abwesend.privatecontacts.view.permission.PermissionRequestResult
import ch.abwesend.privatecontacts.view.permission.PermissionRequestResult.ALREADY_GRANTED
import ch.abwesend.privatecontacts.view.permission.PermissionRequestResult.DENIED
import ch.abwesend.privatecontacts.view.permission.PermissionRequestResult.NEWLY_GRANTED
import ch.abwesend.privatecontacts.view.permission.PermissionRequestResult.PARTIALLY_NEWLY_GRANTED

@Composable
fun ComponentActivity.CallPermissionHandler(
    settings: ISettingsState,
    permissionHelper: PermissionHelper,
    onPermissionsHandled: () -> Unit,
) {
    var requestPermissions by remember { mutableStateOf(false) }

    LaunchedEffect(settings) { // check again when settings change
        if (!callIdentificationPossible) {
            Settings.repository.observeIncomingCalls = false
            Settings.repository.requestIncomingCallPermissions = false
            onPermissionsHandled()
            return@LaunchedEffect
        }

        logger.debug("Checking permissions for caller identification")

        requestPermissionsForCallerIdentification(
            settings = settings,
            permissionHelper = permissionHelper,
            showExplanation = { requestPermissions = true }
        ) {
            if (it == ALREADY_GRANTED) {
                onPermissionsHandled()
            }
        }
    }

    IncomingCallPermissionDialog(
        settings = settings,
        permissionHelper = permissionHelper,
        showDialog = requestPermissions
    ) {
        requestPermissions = false
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
    if (!settings.requestIncomingCallPermissions) {
        return
    }

    val onPermissionResult: (PermissionRequestResult) -> Unit = { result ->
        when (result) {
            NEWLY_GRANTED, PARTIALLY_NEWLY_GRANTED -> {
                Settings.repository.observeIncomingCalls = true
                Toast.makeText(this, R.string.thank_you, Toast.LENGTH_SHORT).show()
                val postfix = if (result == NEWLY_GRANTED) "" else " partially"
                logger.debug("Call detection: permission/role granted$postfix")
            }
            DENIED -> {
                Settings.repository.observeIncomingCalls = false
                logger.debug("Call detection: permission/role denied")
            }
            ALREADY_GRANTED -> {
                logger.debug("Call detection: permission/role already granted")
            }
        }
        onResult?.invoke(result)
    }

    if (canUseCallScreeningService) {
        requestCallScreeningServiceRole(
            permissionHelper = permissionHelper,
            showExplanation = showExplanation,
        ) { serviceRolePermissionResult ->
            if (serviceRolePermissionResult.usable) {
                requestPhoneStatePermission(
                    permissionHelper = permissionHelper,
                    showExplanation = showExplanation,
                ) { phoneStatePermissionResult ->
                    logger.debug("Permission result for phone-state with call-screening: $phoneStatePermissionResult")
                    onPermissionResult(serviceRolePermissionResult) // this is actually what counts more...
                }
            } else {
                onPermissionResult(serviceRolePermissionResult)
            }
        }
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
