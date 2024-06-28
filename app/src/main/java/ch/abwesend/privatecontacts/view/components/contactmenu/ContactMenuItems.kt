package ch.abwesend.privatecontacts.view.components.contactmenu

import android.net.Uri
import androidx.annotation.StringRes
import androidx.compose.material.DropdownMenuItem
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
import ch.abwesend.privatecontacts.view.model.ContactTypeChangeMenuConfig

@Composable
fun ChangeContactTypeMenuItem(
    contacts: Set<IContactBaseWithAccountInformation>,
    config: ContactTypeChangeMenuConfig,
    enabled: Boolean,
    onCloseMenu: (changeContactType: Boolean) -> Unit,
) {
    @StringRes val labelRes = if (contacts.size == 1) config.menuTextSingularRes
    else config.menuTextPluralRes

    var confirmationDialogVisible: Boolean by remember { mutableStateOf(false) }

    DropdownMenuItem(enabled = enabled, onClick = { confirmationDialogVisible = true }) {
        Text(stringResource(id = labelRes))
    }

    ChangeContactTypeConfirmationDialog(
        contacts = contacts,
        visible = confirmationDialogVisible,
        config = config,
        hideDialog = { changeContacts ->
            confirmationDialogVisible = false
            onCloseMenu(changeContacts)
        },
    )
}

@Composable
fun DeleteContactMenuItem(
    numberOfContacts: Int,
    onCloseMenu: (delete: Boolean) -> Unit,
) {
    var dialogVisible: Boolean by remember { mutableStateOf(false) }
    val multipleContacts = numberOfContacts > 1

    DropdownMenuItem(onClick = { dialogVisible = true }) {
        @StringRes val text = if (multipleContacts) R.string.delete_contacts else R.string.delete_contact
        Text(stringResource(id = text))
    }

    DeleteContactConfirmationDialog(
        numberOfContacts = numberOfContacts,
        visible = dialogVisible,
        hideDialog = { delete ->
            dialogVisible = false
            onCloseMenu(delete)
        },
    )
}

@ExperimentalMaterialApi
@Composable
fun ExportContactsMenuItem(
    contacts: Set<IContactBase>,
    onExportContact: (targetFile: Uri, vCardVersion: VCardVersion) -> Unit,
    onCancel: () -> Unit,
) {
    var dialogVisible: Boolean by remember { mutableStateOf(false) }
    val multipleContacts = contacts.size > 1

    DropdownMenuItem(onClick = { dialogVisible = true }) {
        @StringRes val text = if (multipleContacts) R.string.export_contacts else R.string.export_contact
        Text(stringResource(id = text))
    }

    ExportContactConfirmationDialog(
        contacts = contacts,
        visible = dialogVisible,
        onCancel = onCancel,
        onExport = { targetFile, vCardVersion ->
            dialogVisible = false
            onExportContact(targetFile, vCardVersion)
        }
    )
}
