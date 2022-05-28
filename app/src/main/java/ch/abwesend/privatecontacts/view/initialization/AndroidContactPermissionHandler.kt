/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.initialization

import android.Manifest.permission.READ_CONTACTS
import android.Manifest.permission.WRITE_CONTACTS
import android.widget.Toast
import androidx.activity.ComponentActivity
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
import ch.abwesend.privatecontacts.view.components.dialogs.YesNoNeverDialog
import ch.abwesend.privatecontacts.view.permission.PermissionHelper
import ch.abwesend.privatecontacts.view.permission.PermissionRequestResult
import ch.abwesend.privatecontacts.view.permission.PermissionRequestResult.ALREADY_GRANTED
import ch.abwesend.privatecontacts.view.permission.PermissionRequestResult.DENIED
import ch.abwesend.privatecontacts.view.permission.PermissionRequestResult.NEWLY_GRANTED
import ch.abwesend.privatecontacts.view.permission.PermissionRequestResult.PARTIALLY_NEWLY_GRANTED

@Composable
fun ComponentActivity.AndroidContactPermissionHandler(
    settings: ISettingsState,
    permissionHelper: PermissionHelper,
    onPermissionsHandled: () -> Unit,
) {
    var requestPermissions by remember { mutableStateOf(false) }

    LaunchedEffect(settings) { // check again when settings change
        logger.debug("Checking permissions for accessing Android contacts")

        requestAndroidContactPermissions(
            settings = settings,
            permissionHelper = permissionHelper,
            showExplanation = { requestPermissions = true }
        ) {
            if (it == ALREADY_GRANTED) {
                onPermissionsHandled()
            }
        }
    }

    AndroidContactPermissionDialog(
        settings = settings,
        permissionHelper = permissionHelper,
        showDialog = requestPermissions
    ) {
        requestPermissions = false
        onPermissionsHandled()
    }
}

@Composable
private fun ComponentActivity.AndroidContactPermissionDialog(
    settings: ISettingsState,
    permissionHelper: PermissionHelper,
    showDialog: Boolean,
    closeDialog: () -> Unit,
) {
    if (showDialog) {
        YesNoNeverDialog(
            title = R.string.show_android_contacts,
            text = R.string.show_android_contacts_text,
            secondaryTextBlock = R.string.activate_feature,
            onYes = {
                closeDialog()
                requestAndroidContactPermissions(
                    settings = settings,
                    permissionHelper = permissionHelper,
                    showExplanation = null,
                    onResult = null,
                )
            },
            onNo = { doNotShowAgain ->
                closeDialog()
                Settings.repository.showAndroidContacts = false
                if (doNotShowAgain) {
                    Settings.repository.requestAndroidContactPermissions = false
                }
            }
        )
    }
}

private fun ComponentActivity.requestAndroidContactPermissions(
    settings: ISettingsState,
    permissionHelper: PermissionHelper,
    showExplanation: (() -> Unit)?,
    onResult: ((PermissionRequestResult) -> Unit)?,
) {
    if (!settings.requestAndroidContactPermissions) {
        return
    }

    val onPermissionResult: (PermissionRequestResult) -> Unit = { result ->
        when (result) {
            NEWLY_GRANTED, PARTIALLY_NEWLY_GRANTED -> {
                Settings.repository.showAndroidContacts = true
                Toast.makeText(this, R.string.thank_you, Toast.LENGTH_SHORT).show()
                val postfix = if (result == NEWLY_GRANTED) "" else " partially"
                logger.debug("Android contacts: permissions granted$postfix")
            }
            DENIED -> {
                Settings.repository.showAndroidContacts = false
                logger.debug("Android contacts: permissions denied")
            }
            ALREADY_GRANTED -> {
                logger.debug("Android contacts: permissions already granted")
            }
        }
        onResult?.invoke(result)
    }

    requestAndroidContactPermissions(
        permissionHelper = permissionHelper,
        showExplanation = showExplanation,
        onPermissionResult = onPermissionResult,
    )
}

private fun ComponentActivity.requestAndroidContactPermissions(
    permissionHelper: PermissionHelper,
    showExplanation: (() -> Unit)?,
    onPermissionResult: (PermissionRequestResult) -> Unit,
) {
    val permissions = listOf(READ_CONTACTS, WRITE_CONTACTS)

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
