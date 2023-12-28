package ch.abwesend.privatecontacts.view.components.contactmenu

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.view.components.dialogs.SimpleProgressDialog

@Composable
fun DeleteContactsLoadingDialog(deleteMultiple: Boolean) {
    if (deleteMultiple) { // deleting one contact is so fast, that a loading-screen does not make sense
        @StringRes val title = R.string.delete_contacts_progress
        SimpleProgressDialog(title = title, allowRunningInBackground = false)
    }
}

@Composable
fun ChangeContactTypeLoadingDialog(changeMultiple: Boolean) {
    if (changeMultiple) { // changing one contact is so fast, a loading-screen does not make sense
        SimpleProgressDialog(title = R.string.type_change_progress, allowRunningInBackground = false)
    }
}

@Composable
fun ExportContactsLoadingDialog(exportMultiple: Boolean) {
    if (exportMultiple) { // exporting one contact is so fast, that a loading-screen does not make sense
        @StringRes val title = R.string.export_contacts_progress
        SimpleProgressDialog(title = title, allowRunningInBackground = false)
    }
}
