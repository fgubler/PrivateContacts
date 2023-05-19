/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.screens.importexport

import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.contact.ContactAccount
import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.model.contact.ContactType.PUBLIC
import ch.abwesend.privatecontacts.domain.model.contact.ContactType.SECRET
import ch.abwesend.privatecontacts.view.components.buttons.EditIconButton
import ch.abwesend.privatecontacts.view.components.buttons.SecondaryButton
import ch.abwesend.privatecontacts.view.components.inputs.AccountSelectionDropDownField
import ch.abwesend.privatecontacts.view.components.inputs.ContactTypeField
import ch.abwesend.privatecontacts.view.screens.importexport.ImportExportScreenComponents.ImportExportCategory
import ch.abwesend.privatecontacts.view.screens.importexport.extensions.OpenDocumentContract
import ch.abwesend.privatecontacts.view.screens.importexport.extensions.getFilePathForDisplay
import ch.abwesend.privatecontacts.view.viewmodel.ImportExportViewModel
import kotlin.contracts.ExperimentalContracts

@ExperimentalMaterialApi
@ExperimentalContracts
object ImportCategoryComponent {
    private val VCF_MIME_TYPES = arrayOf("text/vcard", "text/x-vcard")
    private val parent = ImportExportScreen // TODO remove once google issue 212091796 is fixed

    @Composable
    fun ImportCategory(viewModel: ImportExportViewModel) {
        val filePath = viewModel.importFileUri.value.getFilePathForDisplay()

        var targetType: ContactType by remember { mutableStateOf(SECRET) }
        val defaultAccount = ContactAccount.currentDefaultForContactType(targetType)
        var selectedAccount: ContactAccount by remember(targetType) { mutableStateOf(defaultAccount) }

        ImportExportCategory(title = R.string.import_title) {
            VcfFilePicker(filePath) { uri ->
                viewModel.setImportFile(uri)
            }

            Spacer(modifier = Modifier.height(10.dp))

            ContactTypeField(selectedType = targetType, isScrolling = { parent.isScrolling }) { newType ->
                targetType = newType
            }

            when (targetType) {
                SECRET -> Unit
                PUBLIC -> {
                    Spacer(modifier = Modifier.height(5.dp))
                    AccountSelectionDropDownField(defaultAccount = defaultAccount) { newValue ->
                        selectedAccount = newValue
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            SecondaryButton(
                enabled = viewModel.importFileUri.value != null,
                onClick = { viewModel.importContacts(targetType, selectedAccount) },
            ) {
                Text(
                    text = stringResource(id = R.string.import_contacts),
                    textAlign = TextAlign.Center
                )
            }
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
}
