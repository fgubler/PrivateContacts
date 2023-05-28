/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.screens.importexport

import android.net.Uri
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.model.contact.ContactAccount
import ch.abwesend.privatecontacts.domain.model.contact.ContactType.PUBLIC
import ch.abwesend.privatecontacts.domain.model.contact.ContactType.SECRET
import ch.abwesend.privatecontacts.view.components.buttons.SecondaryButton
import ch.abwesend.privatecontacts.view.components.dialogs.SimpleProgressDialog
import ch.abwesend.privatecontacts.view.components.inputs.AccountSelectionDropDownField
import ch.abwesend.privatecontacts.view.components.inputs.ContactTypeField
import ch.abwesend.privatecontacts.view.components.inputs.OpenFileFilePicker
import ch.abwesend.privatecontacts.view.screens.importexport.ImportExportScreenComponents.ImportExportCategory
import ch.abwesend.privatecontacts.view.screens.importexport.extensions.ImportExportConstants.VCF_MIME_TYPES
import ch.abwesend.privatecontacts.view.screens.importexport.extensions.getFilePathForDisplay
import ch.abwesend.privatecontacts.view.viewmodel.ImportExportViewModel
import kotlin.contracts.ExperimentalContracts

@ExperimentalMaterialApi
@ExperimentalContracts
object ImportCategoryComponent {
    private val parent = ImportExportScreen // TODO remove once google issue 212091796 is fixed

    @Composable
    fun ImportCategory(viewModel: ImportExportViewModel) {
        val filePath = viewModel.importFileUri.value.getFilePathForDisplay()

        val targetType = viewModel.importTargetType.value
        val defaultAccount = ContactAccount.currentDefaultForContactType(targetType)
        val selectedAccount = viewModel.importTargetAccount.value ?: defaultAccount

        ImportExportCategory(title = R.string.import_title) {
            VcfFilePicker(filePath) { uri ->
                // in the case of "cancel", leave the old value
                uri?.let { viewModel.setImportFile(it) }
            }

            Spacer(modifier = Modifier.height(10.dp))

            ContactTypeField(
                labelRes = R.string.import_contacts_as,
                selectedType = targetType,
                isScrolling = { parent.isScrolling }
            ) { newType -> viewModel.setImportTargetType(newType) }

            when (targetType) {
                SECRET -> Unit
                PUBLIC -> {
                    Spacer(modifier = Modifier.height(5.dp))
                    AccountSelectionDropDownField(defaultAccount = defaultAccount) { newValue ->
                        viewModel.setImportTargetAccount(newValue)
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
        OpenFileFilePicker(
            labelRes = R.string.select_file_to_import,
            mimeTypes = VCF_MIME_TYPES,
            selectedFilePath = selectedFilePath,
            onFileSelected = onFileSelected,
        )
    }

    @Composable
    private fun ProgressDialog() {
        SimpleProgressDialog(title = R.string.import_contacts_progress, allowRunningInBackground = false)
    }
}
