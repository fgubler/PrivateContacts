/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.screens.importexport

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.model.contact.ContactType.PUBLIC
import ch.abwesend.privatecontacts.domain.model.contact.ContactType.SECRET
import ch.abwesend.privatecontacts.view.components.buttons.EditIconButton
import ch.abwesend.privatecontacts.view.components.buttons.SecondaryButton
import ch.abwesend.privatecontacts.view.screens.importexport.extensions.getFilePathForDisplay
import ch.abwesend.privatecontacts.view.viewmodel.ImportExportViewModel

object ImportCategory {
    private val VCF_MIME_TYPES = arrayOf("text/vcard", "text/x-vcard")

    @Composable
    fun ImportCategory(viewModel: ImportExportViewModel) {
        val filePath = viewModel.importFileUri.value.getFilePathForDisplay()

        ImportExportScreenComponents.ImportExportCategory(title = R.string.import_title) {
            VcfFilePicker(filePath) { uri ->
                viewModel.setImportFile(uri)
            }

            Spacer(modifier = Modifier.height(10.dp))

            ImportButtons(viewModel)
        }
    }

    @Composable
    private fun VcfFilePicker(selectedFilePath: String, onFileSelected: (Uri?) -> Unit) {
        val launcher = rememberLauncherForActivityResult(
            contract = OpenDocumentContract(),
            onResult = onFileSelected,
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                label = { Text(text = stringResource(id = R.string.select_file_to_import)) },
                value = selectedFilePath,
                enabled = false,
                modifier = Modifier
                    .weight(1f)
                    .clickable { editImportFilePath(launcher) },
                onValueChange = { newValue ->
                    logger.debug("Changed import file path to $newValue")
                }
            )
            EditIconButton { editImportFilePath(launcher) }
        }
    }

    private fun editImportFilePath(launcher: ManagedActivityResultLauncher<Array<String>, Uri?>) {
        launcher.launch(VCF_MIME_TYPES)
    }

    @Composable
    private fun ImportButtons(viewModel: ImportExportViewModel) {
        Row {
            ImportButton(viewModel = viewModel, type = SECRET, labelRes = R.string.import_as_private_contacts)
            Spacer(modifier = Modifier.width(10.dp))
            ImportButton(viewModel = viewModel, type = PUBLIC, labelRes = R.string.import_as_public_contacts)
        }
    }

    @Composable
    private fun RowScope.ImportButton(viewModel: ImportExportViewModel, type: ContactType, @StringRes labelRes: Int) {
        SecondaryButton(
            enabled = viewModel.importFileUri.value != null,
            modifier = Modifier.weight(1f),
            onClick = { viewModel.importContacts(targetType = type) },
        ) {
            Text(
                text = stringResource(id = labelRes),
                textAlign = TextAlign.Center
            )
        }
    }
}

// TODO fix and move to its own file or delete
class OpenDocumentContract : ActivityResultContracts.OpenDocument() {
    override fun createIntent(context: Context, input: Array<String>): Intent {
        return super.createIntent(context, input).apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}
