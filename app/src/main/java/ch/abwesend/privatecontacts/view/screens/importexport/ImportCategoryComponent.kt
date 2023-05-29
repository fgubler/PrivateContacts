/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.screens.importexport

import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.model.contact.ContactAccount
import ch.abwesend.privatecontacts.domain.model.contact.ContactType.PUBLIC
import ch.abwesend.privatecontacts.domain.model.contact.ContactType.SECRET
import ch.abwesend.privatecontacts.domain.model.importexport.ContactImportData
import ch.abwesend.privatecontacts.domain.model.importexport.VCardParseError
import ch.abwesend.privatecontacts.domain.model.result.BinaryResult
import ch.abwesend.privatecontacts.domain.model.result.Result
import ch.abwesend.privatecontacts.view.components.buttons.SecondaryButton
import ch.abwesend.privatecontacts.view.components.dialogs.OkDialog
import ch.abwesend.privatecontacts.view.components.dialogs.ResourceFlowProgressAndResultDialog
import ch.abwesend.privatecontacts.view.components.dialogs.SimpleProgressDialog
import ch.abwesend.privatecontacts.view.components.inputs.AccountSelectionDropDownField
import ch.abwesend.privatecontacts.view.components.inputs.ContactTypeField
import ch.abwesend.privatecontacts.view.components.inputs.OpenFileFilePicker
import ch.abwesend.privatecontacts.view.screens.importexport.ImportExportScreenComponents.ImportExportCategory
import ch.abwesend.privatecontacts.view.screens.importexport.extensions.ImportExportConstants.VCF_MIME_TYPES
import ch.abwesend.privatecontacts.view.screens.importexport.extensions.getFilePathForDisplay
import ch.abwesend.privatecontacts.view.viewmodel.ContactImportViewModel
import kotlin.contracts.ExperimentalContracts

@ExperimentalMaterialApi
@ExperimentalContracts
object ImportCategoryComponent {
    private val parent = ContactImportExportScreen // TODO remove once google issue 212091796 is fixed

    @Composable
    fun ImportCategory(viewModel: ContactImportViewModel) {
        val filePath = viewModel.fileUri.value.getFilePathForDisplay()

        val targetType = viewModel.targetType.value
        val defaultAccount = ContactAccount.currentDefaultForContactType(targetType)
        val selectedAccount = viewModel.targetAccount.value ?: defaultAccount

        ImportExportCategory(title = R.string.import_title) {
            VcfFilePicker(filePath) { uri ->
                // in the case of "cancel", leave the old value
                uri?.let { viewModel.selectFile(it) }
            }

            Spacer(modifier = Modifier.height(10.dp))

            ContactTypeField(
                labelRes = R.string.import_contacts_as,
                selectedType = targetType,
                isScrolling = { parent.isScrolling }
            ) { newType -> viewModel.selectTargetType(newType) }

            when (targetType) {
                SECRET -> Unit
                PUBLIC -> {
                    Spacer(modifier = Modifier.height(5.dp))
                    AccountSelectionDropDownField(defaultAccount = defaultAccount) { newValue ->
                        viewModel.selectTargetAccount(newValue)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            SecondaryButton(
                enabled = viewModel.fileUri.value != null,
                onClick = { viewModel.importContacts(targetType, selectedAccount) },
            ) {
                Text(
                    text = stringResource(id = R.string.import_contacts),
                    textAlign = TextAlign.Center
                )
            }
        }

        ProgressAndResultHandler(viewModel = viewModel)
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
    private fun ProgressAndResultHandler(viewModel: ContactImportViewModel) {
        ResourceFlowProgressAndResultDialog(
            flow = viewModel.importResult,
            onCloseDialog = { viewModel.resetImportResult() },
            ProgressDialog = { ProgressDialog() },
            ResultDialog = { result, onClose -> ResultDialog(result, onClose) },
        )
    }

    @Composable
    private fun ProgressDialog() {
        SimpleProgressDialog(title = R.string.import_contacts_progress, allowRunningInBackground = false)
    }

    @Composable
    private fun ResultDialog(importResult: BinaryResult<ContactImportData, VCardParseError>, onClose: () -> Unit) {
        when (importResult) {
            is Result.Error -> ErrorResultDialog(error = importResult.error, onClose = onClose)
            is Result.Success -> SuccessResultDialog(data = importResult.value, onClose = onClose)
        }
    }

    @Composable
    private fun ErrorResultDialog(error: VCardParseError, onClose: () -> Unit) {
        OkDialog(title = R.string.import_failed_title, text = error.label, onClose = onClose)
    }

    @Composable
    private fun SuccessResultDialog(data: ContactImportData, onClose: () -> Unit) {
        val scrollState = rememberScrollState()
        OkDialog(title = R.string.import_complete_title, onClose = onClose) {
            Column(modifier = Modifier.verticalScroll(scrollState)) {
                Row {
                    Text(text = stringResource(id = R.string.newly_imported_contacts), fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(text = data.newImportedContacts.size.toString())
                }
                Row {
                    Text(text = stringResource(id = R.string.ignored_existing_contacts), fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(text = data.existingIgnoredContacts.size.toString())
                }
                Row {
                    Text(text = stringResource(id = R.string.vcf_parsing_errors), fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(text = data.numberOfParsingFailures.toString())
                }
                Row {
                    Text(text = stringResource(id = R.string.import_validation_errors), fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(text = data.importValidationFailures.size.toString())
                }
                Row {
                    Text(text = stringResource(id = R.string.import_errors), fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(text = data.importFailures.size.toString())
                }
            }
        }
    }
}
