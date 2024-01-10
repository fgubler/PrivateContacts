/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.screens.settings

import android.widget.Toast
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.lib.logging.LogCache
import ch.abwesend.privatecontacts.domain.settings.Settings
import ch.abwesend.privatecontacts.view.components.buttons.MoreActionsIconButton
import ch.abwesend.privatecontacts.view.components.dialogs.OkDialog
import ch.abwesend.privatecontacts.view.components.dialogs.SimpleProgressDialog
import ch.abwesend.privatecontacts.view.components.dialogs.YesNoDialog
import ch.abwesend.privatecontacts.view.model.DatabaseResetState
import ch.abwesend.privatecontacts.view.model.DatabaseResetState.FAILED
import ch.abwesend.privatecontacts.view.model.DatabaseResetState.INITIAL
import ch.abwesend.privatecontacts.view.model.DatabaseResetState.RUNNING
import ch.abwesend.privatecontacts.view.model.DatabaseResetState.RUNNING_IN_BACKGROUND
import ch.abwesend.privatecontacts.view.model.DatabaseResetState.SUCCESSFUL
import ch.abwesend.privatecontacts.view.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch

@Composable
fun SettingsActions(viewModel: SettingsViewModel) {
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
        Divider()
        CopyLogToClipBoardEntry()
    }

    ConfirmationDialogs(
        viewModel = viewModel,
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
    viewModel: SettingsViewModel,
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

    val coroutineScope = rememberCoroutineScope()
    var databaseResetState by remember { mutableStateOf(INITIAL) }

    if (showDatabaseResetConfirmation) {
        YesNoDialog(
            title = R.string.settings_action_reset_database,
            text = R.string.settings_action_reset_database_confirmation,
            onNo = hideDialogs,
            onYes = {
                hideDialogs()
                databaseResetState = RUNNING
                coroutineScope.launch {
                    val success = viewModel.resetDatabase()
                    databaseResetState = if (success) SUCCESSFUL else FAILED
                }
            },
        )
    }

    DatabaseResetStateDialog(state = databaseResetState) { databaseResetState = it }
}

@Composable
private fun DatabaseResetStateDialog(state: DatabaseResetState, changeState: (DatabaseResetState) -> Unit) {
    when (state) {
        INITIAL, RUNNING_IN_BACKGROUND -> { /* nothing to do */ }
        RUNNING -> {
            SimpleProgressDialog(title = R.string.resetting_database, allowRunningInBackground = false) {
                changeState(RUNNING_IN_BACKGROUND)
            }
        }
        SUCCESSFUL -> {
            OkDialog(
                title = R.string.success,
                text = R.string.resetting_database_successful,
                onClose = { changeState(INITIAL) }
            )
        }
        FAILED -> {
            OkDialog(
                title = R.string.failure,
                text = R.string.resetting_database_failed,
                onClose = { changeState(INITIAL) }
            )
        }
    }
}

@Composable
private fun CopyLogToClipBoardEntry() {
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    DropdownMenuItem(
        onClick = {
            val logs = LogCache.getLog()
            clipboardManager.setText(AnnotatedString(logs))
            Toast.makeText(context, R.string.copied_to_clipboard, Toast.LENGTH_SHORT).show()
        },
        content = { Text(stringResource(id = R.string.settings_action_copy_logs)) }
    )
}
