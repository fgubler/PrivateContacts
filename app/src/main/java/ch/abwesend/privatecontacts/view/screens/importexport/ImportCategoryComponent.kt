/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.screens.importexport

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Checkbox
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.model.contact.ContactAccount
import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.model.importexport.ContactImportData
import ch.abwesend.privatecontacts.domain.model.importexport.VCardParseError
import ch.abwesend.privatecontacts.domain.model.result.generic.BinaryResult
import ch.abwesend.privatecontacts.domain.model.result.generic.ErrorResult
import ch.abwesend.privatecontacts.domain.model.result.generic.SuccessResult
import ch.abwesend.privatecontacts.view.components.buttons.SecondaryButton
import ch.abwesend.privatecontacts.view.components.dialogs.OkDialog
import ch.abwesend.privatecontacts.view.components.dialogs.ResourceFlowProgressAndResultDialog
import ch.abwesend.privatecontacts.view.components.dialogs.SimpleProgressDialog
import ch.abwesend.privatecontacts.view.components.inputs.AccountSelectionDropDownField
import ch.abwesend.privatecontacts.view.components.inputs.ContactTypeField
import ch.abwesend.privatecontacts.view.filepicker.OpenFileFilePickerLauncher.Companion.rememberOpenFileLauncher
import ch.abwesend.privatecontacts.view.permission.IPermissionProvider
import ch.abwesend.privatecontacts.view.screens.importexport.ImportExportScreenComponents.ImportExportCategory
import ch.abwesend.privatecontacts.view.screens.importexport.ImportExportScreenComponents.ImportExportSuccessDialog
import ch.abwesend.privatecontacts.view.screens.importexport.extensions.ActionWithContactPermission.Companion.rememberActionWithContactPermission
import ch.abwesend.privatecontacts.view.screens.importexport.extensions.ImportExportConstants.VCF_MIME_TYPES
import ch.abwesend.privatecontacts.view.util.accountSelectionRequired
import ch.abwesend.privatecontacts.view.viewmodel.ContactImportViewModel
import kotlin.contracts.ExperimentalContracts

@ExperimentalMaterialApi
@ExperimentalContracts
object ImportCategoryComponent {
    @Composable
    fun ImportCategory(viewModel: ContactImportViewModel, permissionProvider: IPermissionProvider) {
        val targetType = viewModel.targetType.value
        val defaultAccount = ContactAccount.currentDefaultForContactType(targetType)
        val selectedAccount = viewModel.targetAccount.value ?: defaultAccount
        var replaceExistingContacts: Boolean by remember { mutableStateOf(false) }

        ImportExportCategory(title = R.string.import_title) {
            TargetTypeFields(viewModel, targetType, selectedAccount)

            if (targetType == ContactType.SECRET) {
                Spacer(modifier = Modifier.height(10.dp))
                ReplaceExistingContactsCheckBox(replaceExistingContacts) {
                    replaceExistingContacts = !replaceExistingContacts
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            ImportButton(viewModel, permissionProvider, targetType, selectedAccount, replaceExistingContacts)
        }

        ProgressAndResultHandler(viewModel = viewModel)
    }

    @Composable
    private fun TargetTypeFields(
        viewModel: ContactImportViewModel,
        targetType: ContactType,
        selectedAccount: ContactAccount,
    ) {
        ContactTypeField(labelRes = R.string.import_contacts_as, selectedType = targetType) { newType ->
            viewModel.selectTargetType(newType)
        }

        if (targetType.accountSelectionRequired) {
            Spacer(modifier = Modifier.height(5.dp))
            AccountSelectionDropDownField(selectedAccount = selectedAccount) { newValue ->
                viewModel.selectTargetAccount(newValue)
            }
        }
    }

    @Composable
    private fun ReplaceExistingContactsCheckBox(currentValue: Boolean, toggleValue: () -> Unit) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { toggleValue() },
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(id = R.string.replace_existing_contacts_label),
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.body1,
                )
                Text(
                    text = stringResource(id = R.string.replace_existing_contacts_description),
                    fontStyle = FontStyle.Italic,
                    style = MaterialTheme.typography.body2,
                )
            }
            Checkbox(
                checked = currentValue,
                onCheckedChange = { toggleValue() },
                modifier = Modifier.padding(start = 10.dp)
            )
        }
    }

    @Composable
    private fun ImportButton(
        viewModel: ContactImportViewModel,
        permissionProvider: IPermissionProvider,
        targetType: ContactType,
        selectedAccount: ContactAccount,
        replaceExisting: Boolean,
    ) {
        val importAction = rememberActionWithContactPermission(permissionProvider)

        val launcher = rememberOpenFileLauncher(mimeTypes = VCF_MIME_TYPES) { sourceFile ->
            viewModel.importContacts(sourceFile, targetType, selectedAccount, replaceExisting)
        }

        importAction.VisibleComponent()
        SecondaryButton(
            onClick = {
                importAction.executeAction(targetType.androidPermissionRequired) { launcher.launch() }
            },
            content = {
                Text(
                    text = stringResource(id = R.string.import_contacts),
                    textAlign = TextAlign.Center
                )
            }
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
            is ErrorResult -> ErrorResultDialog(error = importResult.error, onClose = onClose)
            is SuccessResult -> SuccessResultDialog(data = importResult.value, onClose = onClose)
        }
    }

    @Composable
    private fun ErrorResultDialog(error: VCardParseError, onClose: () -> Unit) {
        OkDialog(title = R.string.import_failed_title, text = error.label, onClose = onClose)
    }

    @Composable
    private fun SuccessResultDialog(data: ContactImportData, onClose: () -> Unit) {
        var showOverview: Boolean by remember { mutableStateOf(true) }
        val hasErrorDetails = data.importFailures.isNotEmpty() || data.importValidationFailures.isNotEmpty()

        if (showOverview) {
            ImportExportSuccessDialog(
                title = R.string.import_complete_title,
                secondButtonText = R.string.import_show_error_details,
                secondButtonVisible = hasErrorDetails,
                onSecondButton = { showOverview = false },
                onClose = onClose
            ) { SuccessResultOverview(importData = data) }
        } else {
            ImportExportSuccessDialog(
                title = R.string.import_error_details_title,
                secondButtonText = R.string.import_show_result_overview,
                secondButtonVisible = true,
                onSecondButton = { showOverview = true },
                onClose = onClose
            ) { SuccessResultErrorDetails(importData = data) }
        }
    }

    @Composable
    private fun SuccessResultOverview(importData: ContactImportData) {
        val scrollState = rememberScrollState()

        Column(modifier = Modifier.verticalScroll(scrollState)) {
            Row {
                Text(text = stringResource(id = R.string.newly_imported_contacts), fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(5.dp))
                Text(text = importData.newImportedContacts.size.toString())
            }
            Row {
                Text(text = stringResource(id = R.string.replaced_existing_contacts), fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(5.dp))
                Text(text = importData.replacedExistingContacts.size.toString())
            }
            Row {
                Text(text = stringResource(id = R.string.vcf_parsing_errors), fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(5.dp))
                Text(text = importData.numberOfParsingFailures.toString())
            }
            Row {
                Text(text = stringResource(id = R.string.import_validation_errors), fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(5.dp))
                Text(text = importData.importValidationFailures.size.toString())
            }
            Row {
                Text(text = stringResource(id = R.string.import_errors), fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(5.dp))
                Text(text = importData.importFailures.size.toString())
            }
        }
    }

    @Composable
    private fun SuccessResultErrorDetails(importData: ContactImportData) {
        val scrollState = rememberScrollState()
        val failedContactNames = remember(importData) {
            val contacts = (importData.importFailures.keys + importData.importValidationFailures.keys)
                .distinctBy { it.id }
            contacts.map { it.displayName }.sorted()
        }

        Column(modifier = Modifier.verticalScroll(scrollState)) {
            Spacer(modifier = Modifier.height(20.dp))
            Text(text = stringResource(id = R.string.import_failed_contacts))
            val missingNameFallback = stringResource(id = R.string.no_contact_name_fallback)
            failedContactNames
                .map { it.ifBlank { missingNameFallback } }
                .forEach { Text(text = " - $it") }
        }
    }
}
