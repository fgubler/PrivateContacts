/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.components.inputs

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ch.abwesend.privatecontacts.domain.lib.logging.debugLocally
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.view.components.DoneIcon
import ch.abwesend.privatecontacts.view.components.ExclamationIcon
import ch.abwesend.privatecontacts.view.components.buttons.EditIconButton
import ch.abwesend.privatecontacts.view.components.inputs.helper.CreateFileContract
import ch.abwesend.privatecontacts.view.components.inputs.helper.OpenFileContract

@Composable
fun OpenFileFilePicker(
    @StringRes labelRes: Int,
    mimeTypes: Array<String>,
    selectedFilePath: String,
    onFileSelected: (Uri?) -> Unit,
) {
    val launcher = rememberLauncherForActivityResult(
        contract = OpenFileContract(),
        onResult = onFileSelected,
    )

    FilePickerFields(labelRes = labelRes, selectedFilePath = selectedFilePath) {
        launcher.launch(mimeTypes)
    }
}

/**
 * [defaultFileName] the pre-selected filename passed to the Android file-picker
 * [displayFilePath] the file does not exist yet, so we don't have a meaningful path: just show a dummy
 */
@Composable
fun CreateFileFilePicker(
    @StringRes labelRes: Int,
    mimeType: String,
    displayFilePath: String,
    defaultFileName: String,
    onFileSelected: (Uri?) -> Unit,
) {
    val launcher = rememberLauncherForActivityResult(
        contract = CreateFileContract(mimeType),
        onResult = onFileSelected,
    )

    FilePickerFields(labelRes = labelRes, selectedFilePath = displayFilePath) {
        launcher.launch(defaultFileName)
    }
}

@Composable
private fun FilePickerFields(
    @StringRes labelRes: Int,
    selectedFilePath: String,
    onOpenFilePicker: () -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            label = { Text(text = stringResource(id = labelRes)) },
            value = selectedFilePath,
            enabled = false,
            modifier = Modifier
                .weight(1f)
                .clickable { onOpenFilePicker() },
            leadingIcon = {
                if (selectedFilePath.isNotEmpty()) DoneIcon() else ExclamationIcon()
            },
            onValueChange = { newValue ->
                logger.debugLocally("Changed file path to $newValue")
                // the actual logic of doing something with the result is implemented by the caller
            },
        )
        EditIconButton { onOpenFilePicker() }
    }
}
