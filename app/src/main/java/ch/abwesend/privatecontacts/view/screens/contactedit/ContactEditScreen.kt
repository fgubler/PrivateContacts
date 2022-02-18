package ch.abwesend.privatecontacts.view.screens.contactedit

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.model.contact.IContactEditable
import ch.abwesend.privatecontacts.domain.model.result.ContactSaveResult
import ch.abwesend.privatecontacts.domain.model.result.ContactSaveResult.Failure
import ch.abwesend.privatecontacts.domain.model.result.ContactSaveResult.Success
import ch.abwesend.privatecontacts.domain.model.result.ContactSaveResult.ValidationFailure
import ch.abwesend.privatecontacts.domain.model.result.ContactSavingError
import ch.abwesend.privatecontacts.domain.model.result.ContactValidationError
import ch.abwesend.privatecontacts.view.components.FullScreenError
import ch.abwesend.privatecontacts.view.components.buttons.CancelIconButton
import ch.abwesend.privatecontacts.view.components.buttons.ExpandCompressIconButton
import ch.abwesend.privatecontacts.view.components.buttons.SaveIconButton
import ch.abwesend.privatecontacts.view.components.dialogs.OkDialog
import ch.abwesend.privatecontacts.view.components.dialogs.YesNoDialog
import ch.abwesend.privatecontacts.view.model.ScreenContext
import ch.abwesend.privatecontacts.view.model.config.ButtonConfig
import ch.abwesend.privatecontacts.view.screens.contactedit.ContactEditScreenContent.ContactEditContent
import ch.abwesend.privatecontacts.view.viewmodel.ContactEditViewModel
import kotlinx.coroutines.flow.collect

@ExperimentalComposeUiApi
@ExperimentalMaterialApi
object ContactEditScreen {
    var isScrolling: Boolean = false // TODO remove once google issue 212091796 is fixed

    @Composable
    fun Screen(screenContext: ScreenContext) {
        val viewModel = screenContext.contactEditViewModel
        val selectedContact = viewModel.selectedContact

        var showAllFields: Boolean by remember { mutableStateOf(true) }
        var showDiscardConfirmationDialog: Boolean by remember { mutableStateOf(false) }
        var savingError: ContactSavingError? by remember { mutableStateOf(null) }
        var validationErrors: List<ContactValidationError> by remember { mutableStateOf(emptyList()) }

        LaunchedEffect(Unit) {
            viewModel.saveResult.collect { result ->
                showDiscardConfirmationDialog = false
                onSaveResult(
                    screenContext = screenContext,
                    result = result,
                    setSavingError = { savingError = it },
                    setValidationErrors = { validationErrors = it },
                )
            }
        }

        selectedContact?.let { contact ->
            Scaffold(
                topBar = {
                    ContactEditTopBar(
                        screenContext = screenContext,
                        contact = contact,
                        expanded = showAllFields,
                        showDiscardConfirmationDialog = { showDiscardConfirmationDialog = true },
                        onToggleExpanded = { showAllFields = !showAllFields }
                    )
                }
            ) {
                ContactEditContent(screenContext, contact, showAllFields)

                // Dialogs
                DiscardConfirmationDialog(screenContext, showDiscardConfirmationDialog) {
                    showDiscardConfirmationDialog = false
                }
                SavingErrorDialog(savingError) {
                    savingError = null
                }
                ValidationErrorDialog(validationErrors) {
                    validationErrors = emptyList()
                }
            }
        } ?: NoContactLoaded(viewModel)
    }

    @Composable
    private fun ContactEditTopBar(
        screenContext: ScreenContext,
        contact: IContactEditable,
        expanded: Boolean,
        showDiscardConfirmationDialog: () -> Unit,
        onToggleExpanded: () -> Unit,
    ) {
        @StringRes val title = if (contact.isNew) R.string.screen_contact_edit_create
        else R.string.screen_contact_edit

        TopAppBar(
            title = { Text(text = stringResource(id = title)) },
            navigationIcon = {
                CancelIconButton { onDiscard(screenContext, showDiscardConfirmationDialog) }
            },
            actions = {
                ExpandCompressIconButton(expanded = expanded, onClick = onToggleExpanded)
                SaveIconButton { onSave(screenContext, contact) }
            }
        )
    }

    private fun onSave(screenContext: ScreenContext, contact: IContactEditable) {
        screenContext.contactEditViewModel.saveContact(contact)
    }

    private fun onSaveResult(
        screenContext: ScreenContext,
        result: ContactSaveResult,
        setValidationErrors: (List<ContactValidationError>) -> Unit,
        setSavingError: (ContactSavingError) -> Unit,
    ) {
        when (result) {
            is Success -> screenContext.router.navigateUp()
            is Failure -> setSavingError(result.error)
            is ValidationFailure -> setValidationErrors(result.validationErrors)
        }
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
            it.originalContact != it.selectedContact?.contact
        }

    @Composable
    private fun DiscardConfirmationDialog(
        screenContext: ScreenContext,
        visible: Boolean,
        hideDialog: () -> Unit
    ) {
        if (visible) {
            YesNoDialog(
                title = R.string.discard_changes_title,
                text = R.string.discard_changes_text,
                onYes = { onDiscardConfirmed(screenContext) },
                onNo = hideDialog
            )
        }
    }

    @Composable
    private fun SavingErrorDialog(
        error: ContactSavingError?,
        onClose: () -> Unit
    ) {
        error?.let { savingError ->
            OkDialog(
                title = R.string.error,
                onClose = onClose
            ) {
                val errorText = stringResource(id = savingError.label)
                val description = stringResource(R.string.saving_data_error, errorText)
                Text(text = description)
            }
        }
    }

    @Composable
    private fun ValidationErrorDialog(
        validationErrors: List<ContactValidationError>,
        onClose: () -> Unit
    ) {
        if (validationErrors.isNotEmpty()) {
            OkDialog(
                title = R.string.data_validation_errors,
                onClose = onClose
            ) {
                Column {
                    validationErrors.forEach { error ->
                        Text(stringResource(id = error.label))
                        Spacer(modifier = Modifier.height(5.dp))
                    }
                }
            }
        }
    }

    @Composable
    private fun NoContactLoaded(viewModel: ContactEditViewModel) {
        FullScreenError(
            errorMessage = R.string.no_contact_selected,
            buttonConfig = ButtonConfig(
                label = R.string.create_contact,
                icon = Icons.Default.Add
            ) {
                viewModel.createContact()
            }
        )
    }
}
