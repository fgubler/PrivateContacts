/*
 * Private Contacts
 * Copyright (c) 2026.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.screens.importexport

import android.net.Uri
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.lib.flow.ResourceFlow
import ch.abwesend.privatecontacts.domain.model.importexport.ContactImportPartialData.ParsedData
import ch.abwesend.privatecontacts.domain.model.importexport.VCardImportError
import ch.abwesend.privatecontacts.domain.model.result.generic.BinaryResult
import ch.abwesend.privatecontacts.domain.model.result.generic.ErrorResult
import ch.abwesend.privatecontacts.domain.model.result.generic.SuccessResult
import ch.abwesend.privatecontacts.view.components.buttons.SecondaryButton
import ch.abwesend.privatecontacts.view.components.dialogs.OkDialog
import ch.abwesend.privatecontacts.view.components.dialogs.PasswordInputDialog
import ch.abwesend.privatecontacts.view.components.dialogs.ResourceFlowProgressAndResultDialog
import ch.abwesend.privatecontacts.view.components.dialogs.SimpleProgressDialog
import ch.abwesend.privatecontacts.view.filepicker.OpenFileFilePickerLauncher.Companion.rememberOpenFileLauncher
import ch.abwesend.privatecontacts.view.model.screencontext.IContactImportExportScreenContext
import ch.abwesend.privatecontacts.view.screens.importexport.ImportExportScreenComponents.ImportExportCategory
import ch.abwesend.privatecontacts.view.screens.importexport.extensions.ImportExportConstants.ALL_BACKUP_MIME_TYPES
import ch.abwesend.privatecontacts.view.screens.importexport.extensions.ImportExportConstants.CRYPT_FILE_EXTENSION
import kotlin.contracts.ExperimentalContracts

@ExperimentalMaterialApi
@ExperimentalContracts
object PeriodicBackupCategoryComponent {

    @Composable
    fun PeriodicBackupCategory(screenContext: IContactImportExportScreenContext) {
        val viewModel = screenContext.importViewModel

        ImportExportCategory(title = R.string.backup_title) {
            Text(text = stringResource(id = R.string.backup_manage_in_settings_description))

            Spacer(modifier = Modifier.height(10.dp))
            SecondaryButton(
                onClick = { screenContext.navigateToSettingsScreen() },
                content = {
                    Text(
                        text = stringResource(id = R.string.backup_go_to_settings),
                        textAlign = TextAlign.Center,
                    )
                }
            )

            Spacer(modifier = Modifier.height(10.dp))
            ValidateBackupButton(viewModel)
        }

        ValidationResultHandler(
            validationResult = viewModel.validationResult,
            onResetResult = { viewModel.resetValidationResult() },
        )
    }

    @Composable
    private fun ValidateBackupButton(viewModel: ch.abwesend.privatecontacts.view.viewmodel.ContactImportViewModel) {
        var showFilePickerErrorDialog: Boolean by remember { mutableStateOf(false) }
        var showPasswordDialog: Boolean by remember { mutableStateOf(false) }
        var pendingFileUri: Uri? by remember { mutableStateOf(null) }

        val launcher = rememberOpenFileLauncher(mimeTypes = ALL_BACKUP_MIME_TYPES) { selectedFile ->
            if (selectedFile != null && selectedFile.path?.endsWith(CRYPT_FILE_EXTENSION) == true) {
                pendingFileUri = selectedFile
                showPasswordDialog = true
            } else {
                viewModel.validateBackup(selectedFile)
            }
        }

        if (showPasswordDialog) {
            PasswordInputDialog(
                title = R.string.backup_encryption_password_dialog_title,
                label = R.string.backup_encryption_password_label,
                onConfirm = { password ->
                    viewModel.validateBackup(pendingFileUri, decryptionPassword = password)
                    showPasswordDialog = false
                    pendingFileUri = null
                },
                onCancel = {
                    showPasswordDialog = false
                    pendingFileUri = null
                },
            )
        }

        SecondaryButton(
            onClick = { showFilePickerErrorDialog = !launcher.launch() },
            content = {
                Text(
                    text = stringResource(id = R.string.backup_validate_button),
                    textAlign = TextAlign.Center,
                )
            }
        )

        if (showFilePickerErrorDialog) {
            OkDialog(title = R.string.unexpected_error, text = R.string.file_picker_error) {
                showFilePickerErrorDialog = false
            }
        }
    }

    @Composable
    private fun ValidationResultHandler(
        validationResult: ResourceFlow<BinaryResult<ParsedData, VCardImportError>>,
        onResetResult: () -> Unit,
    ) {
        ResourceFlowProgressAndResultDialog(
            flow = validationResult,
            onCloseDialog = onResetResult,
            ProgressDialog = { SimpleProgressDialog(title = R.string.backup_validate_progress, allowRunningInBackground = false) },
            ResultDialog = { result, onClose -> ValidationResultDialog(result, onClose) },
        )
    }

    @Composable
    private fun ValidationResultDialog(
        result: BinaryResult<ParsedData, VCardImportError>,
        onClose: () -> Unit,
    ) {
        when (result) {
            is ErrorResult -> OkDialog(title = R.string.backup_validate_failed_title, text = result.error.label, onClose = onClose)
            is SuccessResult -> {
                val data = result.value
                val contactCount = data.successfulContacts.size
                val parsingErrors = data.numberOfFailedContacts
                OkDialog(title = R.string.backup_validate_success_title, onClose = onClose) {
                    Text(text = stringResource(id = R.string.backup_validate_contacts_found, contactCount))
                    if (parsingErrors > 0) {
                        Spacer(modifier = Modifier.height(5.dp))
                        Text(text = stringResource(id = R.string.backup_validate_parsing_errors, parsingErrors))
                    }
                }
            }
        }
    }
}
