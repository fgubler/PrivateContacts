/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.screens.settings

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.settings.Settings
import ch.abwesend.privatecontacts.view.components.buttons.MoreActionsIconButton
import ch.abwesend.privatecontacts.view.components.dialogs.YesNoDialog

@Composable
fun SettingsActions() {
    var expanded: Boolean by remember { mutableStateOf(false) }
    var showRestoreConfirmation: Boolean by remember { mutableStateOf(false) }
    var showDatabaseResetConfirmation: Boolean by remember { mutableStateOf(false) }

    MoreActionsIconButton { expanded = true }
    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        DropdownMenuItem(onClick = { showRestoreConfirmation = true }) {
            Text(stringResource(id = R.string.settings_action_restore_default))
        }
        DropdownMenuItem(onClick = { showDatabaseResetConfirmation = true }) {
            Text(stringResource(id = R.string.settings_action_reset_database))
        }
    }

    ConfirmationDialogs(
        showRestoreConfirmation = showRestoreConfirmation,
        showDatabaseResetConfirmation = showDatabaseResetConfirmation
    ) {
        showRestoreConfirmation = false
        showDatabaseResetConfirmation = false
        expanded = false
    }
}

@Composable
private fun ConfirmationDialogs(
    showRestoreConfirmation: Boolean,
    showDatabaseResetConfirmation: Boolean,
    hideDialogs: () -> Unit,
) {
    if (showRestoreConfirmation) {
        YesNoDialog(
            title = R.string.settings_action_restore_default,
            text = R.string.settings_action_restore_default_confirmation,
            onNo = hideDialogs,
            onYes = {
                hideDialogs()
                Settings.restoreDefaultSettings()
            },
        )
    }

    val context = LocalContext.current
    if (showDatabaseResetConfirmation) {
        YesNoDialog(
            title = R.string.settings_action_reset_database,
            text = R.string.settings_action_reset_database_confirmation,
            onNo = hideDialogs,
            onYes = {
                hideDialogs()
                Toast.makeText(context, "Not yet implemented", Toast.LENGTH_SHORT).show() // TODO implement
            },
        )
    }

    BackHandler(enabled = showRestoreConfirmation || showDatabaseResetConfirmation) {
        hideDialogs()
    }
}
