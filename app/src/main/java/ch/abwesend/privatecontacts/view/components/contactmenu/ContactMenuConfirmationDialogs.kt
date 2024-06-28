package ch.abwesend.privatecontacts.view.components.contactmenu

import android.net.Uri
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.model.contact.IContactBase
import ch.abwesend.privatecontacts.domain.model.contact.IContactBaseWithAccountInformation
import ch.abwesend.privatecontacts.domain.model.importexport.VCardVersion
import ch.abwesend.privatecontacts.view.components.dialogs.OkDialog
import ch.abwesend.privatecontacts.view.components.dialogs.YesNoDialog
import ch.abwesend.privatecontacts.view.components.inputs.VCardVersionField
import ch.abwesend.privatecontacts.view.filepicker.CreateFileFilePickerLauncher
import ch.abwesend.privatecontacts.view.model.ContactTypeChangeMenuConfig
import ch.abwesend.privatecontacts.view.screens.importexport.extensions.ImportExportConstants
import java.time.LocalDate

@Composable
fun ChangeContactTypeConfirmationDialog(
    contacts: Set<IContactBaseWithAccountInformation>,
    visible: Boolean,
    config: ContactTypeChangeMenuConfig,
    hideDialog: (changeContactType: Boolean) -> Unit,
) {
    if (visible) {
        @StringRes val titleRes = if (contacts.size == 1) config.confirmationDialogTitleSingularRes
        else config.confirmationDialogTitlePluralRes
        var saveButtonEnabled by remember { mutableStateOf(true) }

        YesNoDialog(
            title = titleRes,
            text = {
                Column {
                    Text(text = stringResource(id = config.confirmationDialogTextRes))
                    config.ConfirmationDialogAdditionalContent(contacts = contacts) { enabled ->
                        saveButtonEnabled = enabled
                    }
                }
            },
            yesButtonEnabled = saveButtonEnabled,
            onYes = { hideDialog(true) },
            onNo = { hideDialog(false) },
        )
    }
}

@Composable
fun DeleteContactConfirmationDialog(
    numberOfContacts: Int,
    visible: Boolean,
    hideDialog: (delete: Boolean) -> Unit,
) {
    if (visible) {
        @StringRes val titleRes =
            if (numberOfContacts == 1) R.string.delete_contact_title else R.string.delete_contacts_title

        val text = if (numberOfContacts == 1) stringResource(id = R.string.delete_contact_text)
        else stringResource(id = R.string.delete_contacts_text, numberOfContacts)

        YesNoDialog(
            title = titleRes,
            text = { Text(text = text) },
            onYes = { hideDialog(true) },
            onNo = { hideDialog(false) },
        )
    }
}

@ExperimentalMaterialApi
@Composable
fun ExportContactConfirmationDialog(
    contacts: Set<IContactBase>,
    visible: Boolean,
    onExport: (targetFile: Uri, vCardVersion: VCardVersion) -> Unit,
    onCancel: () -> Unit,
) {
    if (visible) {
        val numberOfContacts = contacts.size
        var vCardVersion: VCardVersion by remember { mutableStateOf(VCardVersion.default) }
        var showFilePickerErrorDialog: Boolean by remember { mutableStateOf(false) }

        val defaultFileName = createDefaultExportFileName(contacts, vCardVersion)

        val launcher = CreateFileFilePickerLauncher.rememberCreateFileLauncher(
            mimeType = ImportExportConstants.VCF_MAIN_MIME_TYPE,
            defaultFilename = defaultFileName,
            onFileSelected = { targetFile ->
                targetFile?.let { onExport(it, vCardVersion) }
            },
        )

        @StringRes val titleRes =
            if (numberOfContacts == 1) R.string.export_contact_title else R.string.export_contacts_title

        YesNoDialog(
            title = titleRes,
            yesButtonLabel = R.string.ok,
            noButtonLabel = R.string.cancel,
            onYes = { showFilePickerErrorDialog = !launcher.launch() },
            onNo = onCancel,
            text = {
                VCardVersionField(vCardVersion) { newValue -> vCardVersion = newValue }
            },
        )

        if (showFilePickerErrorDialog) {
            OkDialog(title = R.string.unexpected_error, text = R.string.file_picker_error) {
                onCancel()
                showFilePickerErrorDialog = false
            }
        }
    }
}

@Composable
private fun createDefaultExportFileName(contacts: Set<IContactBase>, vCardVersion: VCardVersion): String {
    val datePrefix = LocalDate.now().toString()
    val versionInfix = vCardVersion.name
    val contactPostfix = if (contacts.size == 1) {
        contacts.first().displayName
            .replace(' ', '_')
            .filter { it.isLetter() || it == '_' }
    } else {
        "${stringResource(id = R.string.selected_for_export)}_${contacts.size}"
    }
    val extension = ImportExportConstants.VCF_FILE_EXTENSION
    return "${datePrefix}_${versionInfix}_$contactPostfix.$extension"
}
