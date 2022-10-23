/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.screens.contactedit

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.OutlinedButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.model.contact.IContactEditable
import ch.abwesend.privatecontacts.domain.model.result.ContactChangeError
import ch.abwesend.privatecontacts.domain.model.result.ContactSaveResult
import ch.abwesend.privatecontacts.domain.model.result.ContactSaveResult.Failure
import ch.abwesend.privatecontacts.domain.model.result.ContactSaveResult.Success
import ch.abwesend.privatecontacts.domain.model.result.ContactSaveResult.ValidationFailure
import ch.abwesend.privatecontacts.domain.model.result.ContactValidationError
import ch.abwesend.privatecontacts.domain.util.Constants
import ch.abwesend.privatecontacts.view.components.FullScreenError
import ch.abwesend.privatecontacts.view.components.buttons.CancelIconButton
import ch.abwesend.privatecontacts.view.components.buttons.ExpandCompressIconButton
import ch.abwesend.privatecontacts.view.components.buttons.SaveIconButton
import ch.abwesend.privatecontacts.view.components.dialogs.OkDialog
import ch.abwesend.privatecontacts.view.components.dialogs.YesNoDialog
import ch.abwesend.privatecontacts.view.model.ScreenContext
import ch.abwesend.privatecontacts.view.model.config.ButtonConfig
import ch.abwesend.privatecontacts.view.screens.contactedit.ContactEditScreenContent.ContactEditContent
import ch.abwesend.privatecontacts.view.theme.GlobalModifiers
import ch.abwesend.privatecontacts.view.viewmodel.ContactEditViewModel

@ExperimentalComposeUiApi
@ExperimentalFoundationApi
@ExperimentalMaterialApi
object ContactEditScreen {
    var isScrolling: Boolean by mutableStateOf(false) // TODO remove once google issue 212091796 is fixed

    @Composable
    fun Screen(screenContext: ScreenContext) {
        val viewModel = screenContext.contactEditViewModel
        val selectedContact = viewModel.selectedContact

        var showAllFields: Boolean by remember { mutableStateOf(true) }

        var showDiscardConfirmationDialog: Boolean by remember { mutableStateOf(false) }
        var savingErrors: List<ContactChangeError> by remember { mutableStateOf(emptyList()) }
        var validationErrors: List<ContactValidationError> by remember { mutableStateOf(emptyList()) }

        LaunchedEffect(Unit) {
            viewModel.saveResult.collect { result ->
                showDiscardConfirmationDialog = false
                onSaveResult(
                    screenContext = screenContext,
                    result = result,
                    setSavingErrors = { savingErrors = it },
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
            ) { padding ->
                // The actual content
                Column(modifier = Modifier.padding(padding)) {
                    ContactEditContent(
                        screenContext = screenContext,
                        contact = contact,
                        showAllFields = showAllFields,
                        modifier = Modifier.weight(1F)
                    )
                    ButtonFooter(screenContext, contact) { showDiscardConfirmationDialog = true }
                }

                // Dialogs
                DiscardConfirmationDialog(screenContext, showDiscardConfirmationDialog) {
                    showDiscardConfirmationDialog = false
                }
                SavingErrorDialog(savingErrors) {
                    savingErrors = emptyList()
                }
                ValidationErrorDialog(validationErrors) {
                    validationErrors = emptyList()
                }

                BackHandler {
                    // trigger discard-changes logic
                    onDiscard(screenContext) {
                        showDiscardConfirmationDialog = true
                    }
                }
            }
        } ?: NoContactLoaded(viewModel)
    }

    @Composable
    private fun ButtonFooter(
        screenContext: ScreenContext,
        contact: IContactEditable,
        modifier: Modifier = Modifier,
        showDiscardDialog: () -> Unit,
    ) {
        val buttonModifier = remember { GlobalModifiers.buttonHeightLarge }
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier
                .fillMaxWidth()
                .padding(15.dp),
        ) {
            OutlinedButton(
                onClick = { onDiscard(screenContext, showDiscardDialog) },
                modifier = buttonModifier.weight(1F),
            ) {
                Text(text = stringResource(id = R.string.cancel))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Button(
                onClick = { onSave(screenContext, contact) },
                modifier = buttonModifier.weight(1F),
            ) {
                Text(text = stringResource(id = R.string.save))
            }
        }
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
        setSavingErrors: (List<ContactChangeError>) -> Unit,
    ) {
        when (result) {
            is Success -> {
                screenContext.contactDetailViewModel.reloadContact() // update data there
                screenContext.router.navigateUp()
            }
            is Failure -> setSavingErrors(result.errors)
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
    private fun SavingErrorDialog(errors: List<ContactChangeError>, onClose: () -> Unit) {
        if (errors.isNotEmpty()) {
            OkDialog(title = R.string.error, onClose = onClose) {
                val description = if (errors.size == 1) {
                    val errorText = stringResource(id = errors.first().label)
                    stringResource(R.string.saving_data_error, errorText)
                } else {
                    val partialTexts = errors.map { stringResource(id = it.label) }
                    val errorText = partialTexts.joinToString(separator = Constants.doubleLinebreak)
                    stringResource(R.string.saving_data_error, errorText)
                }
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
            OkDialog(title = R.string.data_validation_errors, onClose = onClose) {
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
