/*
 * Private Contacts
 * Copyright (c) 2024.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.screens.importexport.backup

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.model.backup.BackupFrequency
import ch.abwesend.privatecontacts.domain.model.backup.BackupScope
import ch.abwesend.privatecontacts.view.components.dialogs.YesNoDialog
import ch.abwesend.privatecontacts.view.components.inputs.DropDownField
import ch.abwesend.privatecontacts.view.model.ResDropDownOption

@ExperimentalMaterialApi
@Composable
fun PeriodicBackupConfigurationDialog(
    currentFrequency: BackupFrequency, // TODO rename or change these argument
    currentScope: BackupScope,
    currentFolderUri: String,
    onSave: (BackupFrequency, BackupScope, String) -> Unit,
    onCancel: () -> Unit,
) {
    var selectedFrequency by remember { mutableStateOf(currentFrequency) }
    var selectedScope by remember { mutableStateOf(currentScope) }
    var selectedFolderUri by remember { mutableStateOf(currentFolderUri) }

    YesNoDialog(
        title = R.string.backup_dialog_title,
        yesButtonLabel = R.string.plan_periodic_backup,
        onYes = { onSave(selectedFrequency, selectedScope, selectedFolderUri) },
        onNo = onCancel,
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                val frequencyOptions = remember {
                    BackupFrequency.entries.map { ResDropDownOption(labelRes = it.label, value = it) }
                }
                val selectedFrequencyOption = remember(selectedFrequency) {
                    ResDropDownOption(labelRes = selectedFrequency.label, value = selectedFrequency)
                }

                DropDownField(
                    labelRes = R.string.backup_frequency,
                    selectedOption = selectedFrequencyOption,
                    options = frequencyOptions,
                    onValueChanged = { selectedFrequency = it }
                )

                Spacer(modifier = Modifier.height(20.dp))

                if (selectedFrequency != BackupFrequency.DISABLED) {
                    val scopeOptions = remember {
                        BackupScope.entries.map { ResDropDownOption(labelRes = it.label, value = it) }
                    }
                    val selectedScopeOption = remember(selectedScope){
                        ResDropDownOption(labelRes = selectedScope.label, value = selectedScope)
                    }

                    DropDownField(
                        labelRes = R.string.backup_scope,
                        selectedOption = selectedScopeOption,
                        options = scopeOptions,
                        onValueChanged = { selectedScope = it }
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // TODO replace the folder-picker field completely! Open the folder-picker when the user presses the OK-button
                }
            }
        },
    )
}