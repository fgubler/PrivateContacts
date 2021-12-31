package ch.abwesend.privatecontacts.view.screens.contactedit

import androidx.annotation.StringRes
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.model.contact.ContactFull
import ch.abwesend.privatecontacts.view.components.FullScreenError
import ch.abwesend.privatecontacts.view.components.buttons.CancelIconButton
import ch.abwesend.privatecontacts.view.components.buttons.SaveIconButton
import ch.abwesend.privatecontacts.view.components.config.ButtonConfig
import ch.abwesend.privatecontacts.view.model.ScreenContext
import ch.abwesend.privatecontacts.view.viewmodel.ContactEditViewModel

@Composable
fun ContactEditScreen(screenContext: ScreenContext) {
    val viewModel = screenContext.contactEditViewModel
    val selectedContact = viewModel.selectedContact

    var showDiscardConfirmationDialog: Boolean by remember { mutableStateOf(false) }

    selectedContact?.let { contact ->
        Scaffold(
            topBar = {
                ContactEditTopBar(screenContext, contact) {
                    showDiscardConfirmationDialog = true
                }
            }
        ) {
            ContactEditContent(screenContext, contact)

            if (showDiscardConfirmationDialog) {
                DiscardConfirmationDialog(screenContext) {
                    showDiscardConfirmationDialog = false
                }
            }
        }
    } ?: NoContactLoaded(viewModel)
}

@Composable
private fun ContactEditTopBar(
    screenContext: ScreenContext,
    contact: ContactFull,
    showDiscardConfirmationDialog: () -> Unit,
) {
    @StringRes val title = if (contact.isNew) R.string.screen_contact_edit_create
    else R.string.screen_contact_edit

    TopAppBar(
        title = { Text(text = stringResource(id = title)) },
        navigationIcon = {
            CancelIconButton { onDiscard(screenContext, showDiscardConfirmationDialog) }
        },
        actions = {
            SaveIconButton { onSave(screenContext, contact) }
        }
    )
}

private fun onSave(screenContext: ScreenContext, contact: ContactFull) {
    screenContext.contactEditViewModel.saveContact(contact)
    screenContext.router.navigateUp()
}

private fun onDiscard(
    screenContext: ScreenContext,
    showConfirmationDialog: () -> Unit,
) {
    if (hasChanges(screenContext)) {
        showConfirmationDialog()
    } else {
        onDiscardConfirmed(screenContext)
    }
}

private fun onDiscardConfirmed(screenContext: ScreenContext) {
    screenContext.router.navigateUp()
    screenContext.contactEditViewModel.clearContact()
}

private fun hasChanges(screenContext: ScreenContext): Boolean =
    screenContext.contactEditViewModel.let {
        it.originalContact != it.selectedContact
    }

@Composable
private fun DiscardConfirmationDialog(
    screenContext: ScreenContext,
    hideConfirmationDialog: () -> Unit
) {
    val onCancel = { hideConfirmationDialog() }
    AlertDialog(
        title = { Text(stringResource(id = R.string.discard_changes_title)) },
        text = { Text(stringResource(id = R.string.discard_changes_text)) },
        confirmButton = {
            Button(onClick = { onDiscardConfirmed(screenContext) }) {
                Text(stringResource(id = R.string.yes))
            }
        },
        dismissButton = {
            Button(onClick = onCancel) {
                Text(stringResource(id = R.string.no))
            }
        },

        onDismissRequest = onCancel
    )
}

@Composable
private fun NoContactLoaded(viewModel: ContactEditViewModel) {
    FullScreenError(
        errorMessage = R.string.no_contact_selected,
        buttonConfig = ButtonConfig(
            label = R.string.create_contact,
            icon = Icons.Default.Add
        ) {
            viewModel.createNewContact()
        }
    )
}
