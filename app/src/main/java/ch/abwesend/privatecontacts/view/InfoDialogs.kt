package ch.abwesend.privatecontacts.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.Settings
import ch.abwesend.privatecontacts.view.components.dialogs.OkDialog

@Composable
fun InfoDialogs() {
    var showInitialAppInfoDialog by remember { mutableStateOf(Settings.showInitialAppInfoDialog) }

    if (showInitialAppInfoDialog) {
        InitialAppInfoDialog { showInitialAppInfoDialog = false }
    }
}

@Composable
private fun InitialAppInfoDialog(close: () -> Unit) {
    OkDialog(
        title = R.string.app_name,
        text = R.string.app_introduction_description,
        onClose = close
    )
}
