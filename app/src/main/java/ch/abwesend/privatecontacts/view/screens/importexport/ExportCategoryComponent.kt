/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.screens.importexport

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.model.importexport.ContactExportData
import ch.abwesend.privatecontacts.domain.model.importexport.VCardCreateError
import ch.abwesend.privatecontacts.domain.model.importexport.VCardVersion
import ch.abwesend.privatecontacts.domain.model.result.generic.BinaryResult
import ch.abwesend.privatecontacts.domain.model.result.generic.ErrorResult
import ch.abwesend.privatecontacts.domain.model.result.generic.SuccessResult
import ch.abwesend.privatecontacts.view.components.buttons.SecondaryButton
import ch.abwesend.privatecontacts.view.components.dialogs.OkDialog
import ch.abwesend.privatecontacts.view.components.dialogs.ResourceFlowProgressAndResultDialog
import ch.abwesend.privatecontacts.view.components.dialogs.SimpleProgressDialog
import ch.abwesend.privatecontacts.view.components.inputs.ContactTypeField
import ch.abwesend.privatecontacts.view.components.inputs.VCardVersionField
import ch.abwesend.privatecontacts.view.filepicker.CreateFileFilePickerLauncher.Companion.rememberCreateFileLauncher
import ch.abwesend.privatecontacts.view.screens.importexport.ImportExportScreenComponents.ImportExportCategory
import ch.abwesend.privatecontacts.view.screens.importexport.ImportExportScreenComponents.ImportExportSuccessDialog
import ch.abwesend.privatecontacts.view.screens.importexport.extensions.ActionWithContactPermission.Companion.rememberActionWithContactPermission
import ch.abwesend.privatecontacts.view.screens.importexport.extensions.ImportExportConstants.VCF_FILE_EXTENSION
import ch.abwesend.privatecontacts.view.screens.importexport.extensions.ImportExportConstants.VCF_MAIN_MIME_TYPE
import ch.abwesend.privatecontacts.view.viewmodel.ContactExportViewModel
import java.time.LocalDate
import kotlin.contracts.ExperimentalContracts

@ExperimentalMaterialApi
@ExperimentalContracts
object ExportCategoryComponent {
    private val parent = ContactImportExportScreen // TODO remove once google issue 212091796 is fixed

    @Composable
    fun ExportCategory(viewModel: ContactExportViewModel) {
        ImportExportCategory(title = R.string.export_title) {
            ExportCategoryContent(viewModel = viewModel)
        }

        ProgressAndResultHandler(viewModel = viewModel)
    }

    @Composable
    private fun ExportCategoryContent(viewModel: ContactExportViewModel) {
        val selectedType = viewModel.sourceType.value

        ContactTypeField(
            labelRes = R.string.contact_type_to_export,
            selectedType = selectedType,
            showInfoButton = false,
            isScrolling = { parent.isScrolling },
        ) { newType -> viewModel.selectSourceType(newType) }

        Spacer(modifier = Modifier.height(10.dp))

        VCardVersionField(
            selectedVersion = viewModel.vCardVersion.value,
            isScrolling = { parent.isScrolling },
        ) { newVersion -> viewModel.selectVCardVersion(newVersion) }

        Spacer(modifier = Modifier.height(10.dp))

        ExportButton(viewModel)
    }

    @Composable
    private fun ExportButton(viewModel: ContactExportViewModel) {
        val sourceType = viewModel.sourceType.value
        val vCardVersion = viewModel.vCardVersion.value

        val defaultFileName = createDefaultFileName(sourceType = sourceType, vCardVersion)
        val exportAction = rememberActionWithContactPermission()

        val launcher = rememberCreateFileLauncher(
            mimeType = VCF_MAIN_MIME_TYPE,
            defaultFilename = defaultFileName,
            onFileSelected = { targetFile -> viewModel.exportContacts(targetFile, sourceType, vCardVersion) },
        )

        exportAction.VisibleComponent()

        SecondaryButton(
            onClick = {
                exportAction.executeAction(sourceType.androidPermissionRequired) { launcher.launch() }
            },
            content = {
                Text(
                    text = stringResource(id = R.string.export_contacts),
                    textAlign = TextAlign.Center
                )
            },
        )
    }

    @Composable
    private fun createDefaultFileName(sourceType: ContactType, vCardVersion: VCardVersion): String {
        val datePrefix = LocalDate.now().toString()
        val versionInfix = vCardVersion.name
        val typePostfix = stringResource(id = sourceType.label)
        val extension = VCF_FILE_EXTENSION
        return "${datePrefix}_${versionInfix}_$typePostfix.$extension"
    }

    @Composable
    private fun ProgressAndResultHandler(viewModel: ContactExportViewModel) {
        ResourceFlowProgressAndResultDialog(
            flow = viewModel.exportResult,
            onCloseDialog = { viewModel.resetExportResult() },
            ProgressDialog = { ProgressDialog() },
            ResultDialog = { result, onClose -> ResultDialog(result, onClose) },
        )
    }

    @Composable
    private fun ProgressDialog() {
        SimpleProgressDialog(title = R.string.export_contacts_progress, allowRunningInBackground = false)
    }

    @Composable
    private fun ResultDialog(exportResult: BinaryResult<ContactExportData, VCardCreateError>, onClose: () -> Unit) {
        when (exportResult) {
            is ErrorResult -> ErrorResultDialog(error = exportResult.error, onClose = onClose)
            is SuccessResult -> SuccessResultDialog(
                data = exportResult.value,
                onClose = onClose
            )
        }
    }

    @Composable
    private fun ErrorResultDialog(error: VCardCreateError, onClose: () -> Unit) {
        OkDialog(title = R.string.export_failed_title, text = error.label, onClose = onClose)
    }

    @Composable
    private fun SuccessResultDialog(data: ContactExportData, onClose: () -> Unit) {
        var showOverview: Boolean by remember { mutableStateOf(true) }
        val hasErrorDetails = data.failedContacts.isNotEmpty()

        if (showOverview) {
            ImportExportSuccessDialog(
                title = R.string.export_complete_title,
                secondButtonText = R.string.import_show_error_details,
                secondButtonVisible = hasErrorDetails,
                onSecondButton = { showOverview = false },
                onClose = onClose
            ) { SuccessResultOverview(data = data) }
        } else {
            ImportExportSuccessDialog(
                title = R.string.import_error_details_title,
                secondButtonText = R.string.import_show_result_overview,
                secondButtonVisible = true,
                onSecondButton = { showOverview = true },
                onClose = onClose
            ) { SuccessResultErrorDetails(exportData = data) }
        }
    }

    @Composable
    private fun SuccessResultOverview(data: ContactExportData) {
        val scrollState = rememberScrollState()

        Column(modifier = Modifier.verticalScroll(scrollState)) {
            Row {
                Text(text = stringResource(id = R.string.exported_contacts), fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(5.dp))
                Text(text = data.successfulContacts.size.toString())
            }
            Row {
                Text(text = stringResource(id = R.string.failed_to_export_contacts), fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(5.dp))
                Text(text = data.failedContacts.size.toString())
            }
        }
    }

    @Composable
    private fun SuccessResultErrorDetails(exportData: ContactExportData) {
        val scrollState = rememberScrollState()
        val failedContactNames = remember(exportData) {
            exportData.failedContacts.map { it.displayName }.sorted()
        }

        Column(modifier = Modifier.verticalScroll(scrollState)) {
            Spacer(modifier = Modifier.height(20.dp))
            Row {
                Text(text = stringResource(id = R.string.export_failed_contacts), fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(5.dp))
                Text(text = exportData.failedContacts.size.toString())
            }
            failedContactNames.forEach { Text(text = " - $it") }
        }
    }
}
