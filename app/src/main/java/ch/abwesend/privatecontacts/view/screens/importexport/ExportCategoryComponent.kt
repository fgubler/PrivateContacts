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
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import ch.abwesend.privatecontacts.view.components.buttons.InfoIconButton
import ch.abwesend.privatecontacts.view.components.buttons.SecondaryButton
import ch.abwesend.privatecontacts.view.components.dialogs.OkDialog
import ch.abwesend.privatecontacts.view.components.dialogs.ResourceFlowProgressAndResultDialog
import ch.abwesend.privatecontacts.view.components.dialogs.SimpleProgressDialog
import ch.abwesend.privatecontacts.view.components.inputs.ContactTypeField
import ch.abwesend.privatecontacts.view.components.inputs.DropDownField
import ch.abwesend.privatecontacts.view.filepicker.CreateFileFilePickerLauncher.Companion.rememberCreateFileLauncher
import ch.abwesend.privatecontacts.view.model.ResDropDownOption
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

        VCardVersionField(viewModel = viewModel)

        Spacer(modifier = Modifier.height(10.dp))

        ExportButton(viewModel)
    }

    @Composable
    private fun VCardVersionField(viewModel: ContactExportViewModel) {
        var showInfoDialog: Boolean by remember { mutableStateOf(false) }
        val selectedVersion = viewModel.vCardVersion.value

        val selectedOption = ResDropDownOption(labelRes = selectedVersion.label, value = selectedVersion)
        val options = VCardVersion.values().map {
            ResDropDownOption(labelRes = it.label, value = it)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(modifier = Modifier.weight(1.0f)) {
                DropDownField(
                    labelRes = R.string.vcard_version_label,
                    selectedOption = selectedOption,
                    options = options,
                    isScrolling = { parent.isScrolling },
                    onValueChanged = { newVersion -> viewModel.selectVCardVersion(newVersion) }
                )
            }

            InfoIconButton { showInfoDialog = true }
        }

        if (showInfoDialog) {
            VCardVersionInfoDialog { showInfoDialog = false }
        }
    }

    @Composable
    private fun VCardVersionInfoDialog(onClose: () -> Unit) {
        OkDialog(
            title = R.string.vcard_versions_label,
            okButtonLabel = R.string.close,
            onClose = onClose,
            content = {
                Column {
                    Text(text = stringResource(id = R.string.vcard_v3_label), fontWeight = FontWeight.Bold)
                    Text(text = stringResource(id = R.string.vcard_v3_info_text))
                    Text(text = stringResource(id = R.string.vcard_v3_info_text_relationships), modifier = Modifier.padding(start = 10.dp))
                    Text(text = stringResource(id = R.string.vcard_v3_info_text_events), modifier = Modifier.padding(start = 10.dp))

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = stringResource(id = R.string.vcard_v4_label), fontWeight = FontWeight.Bold)
                    Text(text = stringResource(id = R.string.vcard_v4_info_text))
                }
            },
        )
    }

    @Composable
    private fun ExportButton(viewModel: ContactExportViewModel) {
        val sourceType = viewModel.sourceType.value
        val vCardVersion = viewModel.vCardVersion.value

        val defaultFileName = createDefaultFileName(sourceType = sourceType)
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
    private fun createDefaultFileName(sourceType: ContactType): String {
        val datePrefix = LocalDate.now().toString()
        val typePostfix = stringResource(id = sourceType.label)
        val extension = VCF_FILE_EXTENSION
        return "${datePrefix}_$typePostfix.$extension"
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
        val hasErrorDetails = false // TODO implement

        // TODO implement
        if (showOverview) {
            ImportExportSuccessDialog(
                title = R.string.export_complete_title,
                secondButtonText = R.string.import_show_error_details,
                secondButtonVisible = hasErrorDetails,
                onSecondButton = { showOverview = false },
                onClose = onClose
            ) { Text("Export Successful") /* TODO implement */ }
        } else {
            ImportExportSuccessDialog(
                title = R.string.import_error_details_title,
                secondButtonText = R.string.import_show_result_overview,
                secondButtonVisible = true,
                onSecondButton = { showOverview = true },
                onClose = onClose
            ) { /* TODO implement*/ }
        }
    }
}
