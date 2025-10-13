/*
 * Private Contacts
 * Copyright (c) 2024.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.screens.importexport

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.model.backup.BackupFrequency
import ch.abwesend.privatecontacts.domain.service.BackupSchedulerService
import ch.abwesend.privatecontacts.domain.settings.Settings
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import ch.abwesend.privatecontacts.view.components.buttons.SecondaryButton
import ch.abwesend.privatecontacts.view.screens.importexport.ImportExportScreenComponents.ImportExportCategory

@ExperimentalMaterialApi
object BackupCategoryComponent {
    @Composable
    fun BackupCategory() {
        ImportExportCategory(title = R.string.backup_title) {
            BackupCategoryContent()
        }
    }

    @Composable
    private fun BackupCategoryContent() {
        val settings by Settings.repository.settings.collectAsState(initial = Settings.current)
        val backupScheduler: BackupSchedulerService by injectAnywhere()
        var showConfigDialog by remember { mutableStateOf(false) }

        Column {
            // Current backup status
            BackupStatusDisplay(
                frequency = settings.backupFrequency,
                scope = settings.backupScope,
                folderUri = settings.backupFolderUri
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Configure backup button
            SecondaryButton(
                onClick = { showConfigDialog = true }
            ) {
                Text(
                    text = stringResource(id = R.string.backup_configure),
                    textAlign = TextAlign.Center
                )
            }
        }

        // Configuration dialog
        if (showConfigDialog) {
            BackupConfigurationDialog(
                currentFrequency = settings.backupFrequency,
                currentScope = settings.backupScope,
                currentFolderUri = settings.backupFolderUri,
                onSave = { frequency, scope, folderUri ->
                    Settings.repository.backupFrequency = frequency
                    Settings.repository.backupScope = scope
                    Settings.repository.backupFolderUri = folderUri
                    backupScheduler.scheduleBackup()
                    showConfigDialog = false
                },
                onCancel = { showConfigDialog = false }
            )
        }
    }

    @Composable
    private fun BackupStatusDisplay(
        frequency: BackupFrequency,
        scope: ch.abwesend.privatecontacts.domain.model.backup.BackupScope,
        folderUri: String
    ) {
        val statusText = if (frequency == BackupFrequency.DISABLED) {
            stringResource(R.string.backup_status_disabled)
        } else {
            val frequencyText = stringResource(frequency.label)
            val scopeText = stringResource(scope.label)
            stringResource(R.string.backup_status_enabled, frequencyText, scopeText)
        }

        Text(text = statusText)
    }
}
