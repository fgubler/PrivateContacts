/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.screens.importexport.extensions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.view.permission.IPermissionProvider
import ch.abwesend.privatecontacts.view.screens.importexport.ImportExportScreenComponents.AndroidPermissionDeniedDialog

/**
 * A helper to check for Android Contacts permissions
 *  - executes a passed action if the permission is granted
 *  - shows an error-dialog otherwise
 *
 *  Beware: make sure to call [VisibleComponent] in your composable function to make sure the dialog can be shown.
 */
class ActionWithContactPermission private constructor(private val permissionProvider: IPermissionProvider) {
    private var showPermissionDeniedDialog: Boolean by mutableStateOf(false)

    @Composable
    fun VisibleComponent() {
        if (showPermissionDeniedDialog) {
            AndroidPermissionDeniedDialog { showPermissionDeniedDialog = false }
        }
    }

    fun executeAction(permissionRequired: Boolean, action: () -> Unit) {
        if (permissionRequired) {
            permissionProvider.contactPermissionHelper.requestAndroidContactPermissions { result ->
                logger.debug("Android contact permissions: $result")
                if (result.usable) { action() } else { showPermissionDeniedDialog = true }
            }
        } else { action() }
    }

    companion object {
        @Composable
        fun rememberActionWithContactPermission(permissionProvider: IPermissionProvider): ActionWithContactPermission =
            remember { ActionWithContactPermission(permissionProvider) }
    }
}
