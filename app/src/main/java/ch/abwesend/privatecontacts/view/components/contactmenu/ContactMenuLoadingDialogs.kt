package ch.abwesend.privatecontacts.view.components.contactmenu

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.view.components.dialogs.SimpleProgressDialog

@Composable
fun DeleteContactsLoadingDialog(deleteMultiple: Boolean) {
    if (deleteMultiple) { // deleting one contact is so fast, a loading-screen does not make sense
        @StringRes val title =
            if (deleteMultiple) R.string.delete_contacts_progress
            else R.string.delete_contact_progress
        SimpleProgressDialog(title = title, allowRunningInBackground = false)
    }
}

@Composable
fun ChangeContactTypeLoadingDialog(changeMultiple: Boolean) {
    if (changeMultiple) { // changing one contact is so fast, a loading-screen does not make sense
        SimpleProgressDialog(title = R.string.type_change_progress, allowRunningInBackground = false)
    }
}
