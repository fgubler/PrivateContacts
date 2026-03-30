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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.model.contact.ContactAccount
import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.view.components.buttons.SecondaryButton
import ch.abwesend.privatecontacts.view.components.dialogs.EditTextDialog
import ch.abwesend.privatecontacts.view.components.dialogs.OkDialog
import ch.abwesend.privatecontacts.view.components.dialogs.PasswordInputDialog
import ch.abwesend.privatecontacts.view.filepicker.OpenFileFilePickerLauncher.Companion.rememberOpenFileLauncher
import ch.abwesend.privatecontacts.view.permission.IPermissionProvider
import ch.abwesend.privatecontacts.view.screens.importexport.ImportComponents.ProgressAndResultHandler
import ch.abwesend.privatecontacts.view.screens.importexport.ImportComponents.ReplaceExistingContactsCheckBox
import ch.abwesend.privatecontacts.view.screens.importexport.ImportComponents.TargetTypeFields
import ch.abwesend.privatecontacts.view.screens.importexport.ImportExportScreenComponents.ImportExportCategory
import ch.abwesend.privatecontacts.view.screens.importexport.extensions.ActionWithContactPermission.Companion.rememberActionWithContactPermission
import ch.abwesend.privatecontacts.view.screens.importexport.extensions.ImportExportConstants.ALL_BACKUP_MIME_TYPES
import ch.abwesend.privatecontacts.view.screens.importexport.extensions.ImportExportConstants.CRYPT_FILE_EXTENSION
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
            TargetTypeFields(
                targetType = targetType,
                selectedAccount = selectedAccount,
                onTargetTypeChanged = { viewModel.selectTargetType(it) },
                onTargetAccountChanged = { viewModel.selectTargetAccount(it) },
            )

            if (targetType == ContactType.SECRET) {
                Spacer(modifier = Modifier.height(10.dp))
                ReplaceExistingContactsCheckBox(replaceExistingContacts) {
                    replaceExistingContacts = !replaceExistingContacts
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            ImportButton(viewModel, permissionProvider, targetType, selectedAccount, replaceExistingContacts)
        }

        ProgressAndResultHandler(
            importResult = viewModel.importResult,
            onResetResult = { viewModel.resetImportResult() }
        )
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
        var showFilePickerErrorDialog: Boolean by remember { mutableStateOf(false) }

        var showPasswordDialog: Boolean by remember { mutableStateOf(false) }
        var pendingFileUri: Uri? by remember { mutableStateOf(null) }

        val launcher = rememberOpenFileLauncher(mimeTypes = ALL_BACKUP_MIME_TYPES) { sourceFile ->
            if (sourceFile != null && sourceFile.path?.endsWith(CRYPT_FILE_EXTENSION) == true) {
                pendingFileUri = sourceFile
                showPasswordDialog = true
            } else {
                viewModel.importContacts(sourceFile, targetType, selectedAccount, replaceExisting)
            }
        }

        if (showPasswordDialog) {
            PasswordInputDialog(
                title = R.string.backup_encryption_password_dialog_title,
                label = R.string.backup_encryption_password_label,
                onConfirm = { password ->
                    viewModel.importContacts(
                        sourceFile = pendingFileUri,
                        targetType = targetType,
                        targetAccount = selectedAccount,
                        replaceExisting = replaceExisting,
                        decryptionPassword = password
                    )
                    showPasswordDialog = false
                    pendingFileUri = null
                },
                onCancel = {
                    showPasswordDialog = false
                    pendingFileUri = null
                },
            )
        }

        importAction.VisibleComponent()
        SecondaryButton(
            onClick = {
                importAction.executeAction(targetType.androidPermissionRequired) {
                    showFilePickerErrorDialog = !launcher.launch()
                }
            },
            content = {
                Text(
                    text = stringResource(id = R.string.import_contacts),
                    textAlign = TextAlign.Center
                )
            }
        )

        if (showFilePickerErrorDialog) {
            OkDialog(title = R.string.unexpected_error, text = R.string.file_picker_error) {
                showFilePickerErrorDialog = false
            }
        }
    }
}
