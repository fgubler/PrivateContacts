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
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.contact.ContactAccount
import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.model.importexport.ContactImportData
import ch.abwesend.privatecontacts.domain.model.importexport.VCardParseError
import ch.abwesend.privatecontacts.domain.model.result.generic.BinaryResult
import ch.abwesend.privatecontacts.domain.model.result.generic.ErrorResult
import ch.abwesend.privatecontacts.domain.model.result.generic.SuccessResult
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import ch.abwesend.privatecontacts.view.components.buttons.SecondaryButton
import ch.abwesend.privatecontacts.view.components.dialogs.OkDialog
import ch.abwesend.privatecontacts.view.components.dialogs.ResourceFlowProgressAndResultDialog
import ch.abwesend.privatecontacts.view.components.dialogs.SimpleProgressDialog
import ch.abwesend.privatecontacts.view.components.inputs.AccountSelectionDropDownField
import ch.abwesend.privatecontacts.view.components.inputs.ContactTypeField
import ch.abwesend.privatecontacts.view.filepicker.OpenFileFilePickerLauncher
import ch.abwesend.privatecontacts.view.permission.AndroidContactPermissionHelper
import ch.abwesend.privatecontacts.view.screens.importexport.ImportExportScreenComponents.ImportExportCategory
import ch.abwesend.privatecontacts.view.screens.importexport.ImportExportScreenComponents.ImportExportSuccessDialog
import ch.abwesend.privatecontacts.view.screens.importexport.extensions.ImportExportConstants.VCF_MIME_TYPES
import ch.abwesend.privatecontacts.view.util.accountSelectionRequired
import ch.abwesend.privatecontacts.view.viewmodel.ContactImportViewModel
import kotlin.contracts.ExperimentalContracts

@ExperimentalMaterialApi
@ExperimentalContracts
object ImportCategoryComponent {
    private val contactPermissionHelper: AndroidContactPermissionHelper by injectAnywhere()

    private val parent = ContactImportExportScreen // TODO remove once google issue 212091796 is fixed

    @Composable
    fun ImportCategory(viewModel: ContactImportViewModel) {
        val targetType = viewModel.targetType.value
        val defaultAccount = ContactAccount.currentDefaultForContactType(targetType)
        val selectedAccount = viewModel.targetAccount.value ?: defaultAccount

        ImportExportCategory(title = R.string.import_title) {
            TargetTypeFields(viewModel, targetType, selectedAccount)
            Spacer(modifier = Modifier.height(10.dp))
            ImportButton(viewModel, targetType, selectedAccount)
        }

        ProgressAndResultHandler(viewModel = viewModel)
    }

    @Composable
    private fun TargetTypeFields(
        viewModel: ContactImportViewModel,
        targetType: ContactType,
        selectedAccount: ContactAccount,
    ) {
        ContactTypeField(
            labelRes = R.string.import_contacts_as,
            selectedType = targetType,
            isScrolling = { parent.isScrolling }
        ) { newType -> viewModel.selectTargetType(newType) }

        if (targetType.accountSelectionRequired) {
            Spacer(modifier = Modifier.height(5.dp))
            AccountSelectionDropDownField(selectedAccount = selectedAccount) { newValue ->
                viewModel.selectTargetAccount(newValue)
            }
        }
    }

    @Composable
    private fun ImportButton(
        viewModel: ContactImportViewModel,
        targetType: ContactType,
        selectedAccount: ContactAccount,
    ) {
        var showPermissionDeniedDialog: Boolean by remember { mutableStateOf(false) }

        val launcher = OpenFileFilePickerLauncher.rememberLauncher(mimeTypes = VCF_MIME_TYPES) { sourceFile ->
            importContacts(
                viewModel = viewModel,
                sourceFile = sourceFile,
                targetType = targetType,
                selectedAccount = selectedAccount,
                onPermissionDenied = { showPermissionDeniedDialog = true }
            )
        }

        SecondaryButton(
            onClick = { launcher.launch() },
            content = {
                Text(
                    text = stringResource(id = R.string.import_contacts),
                    textAlign = TextAlign.Center
                )
            }
        )

        if (showPermissionDeniedDialog) {
            PermissionDeniedDialog { showPermissionDeniedDialog = false }
        }
    }

    private fun importContacts(
        viewModel: ContactImportViewModel,
        sourceFile: Uri?,
        targetType: ContactType,
        selectedAccount: ContactAccount,
        onPermissionDenied: () -> Unit,
    ) {
        if (targetType.androidPermissionRequired) {
            contactPermissionHelper.requestAndroidContactPermissions { result ->
                logger.debug("Android contact permissions: $result")
                if (result.usable) {
                    viewModel.importContacts(sourceFile, targetType, selectedAccount)
                } else {
                    onPermissionDenied()
                }
            }
        } else {
            viewModel.importContacts(sourceFile, targetType, selectedAccount)
        }
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
    private fun PermissionDeniedDialog(closeDialog: () -> Unit) {
        OkDialog(
            title = R.string.import_export_permission_required_title,
            onClose = closeDialog
        ) {
            Column {
                Text(text = stringResource(id = R.string.import_export_permission_required_explanation))
                Spacer(modifier = Modifier.height(10.dp))
                Divider()
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = stringResource(id = R.string.settings_info_dialog_android_contacts_permission),
                    fontStyle = FontStyle.Italic,
                )
            }
        }
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
                Text(text = stringResource(id = R.string.ignored_existing_contacts), fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(5.dp))
                Text(text = importData.existingIgnoredContacts.size.toString())
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
            contacts.map { it.displayName }
        }

        Column(modifier = Modifier.verticalScroll(scrollState)) {
            Spacer(modifier = Modifier.height(20.dp))
            Text(text = stringResource(id = R.string.import_failed_contacts))
            failedContactNames.forEach {
                Text(text = " - $it")
            }
        }
    }
}
