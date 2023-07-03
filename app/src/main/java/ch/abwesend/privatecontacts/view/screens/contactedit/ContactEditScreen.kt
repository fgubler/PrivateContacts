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
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
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
import ch.abwesend.privatecontacts.domain.model.result.ContactChangeError.UNABLE_TO_RESOLVE_EXISTING_CONTACT
import ch.abwesend.privatecontacts.domain.model.result.ContactSaveResult
import ch.abwesend.privatecontacts.domain.model.result.ContactSaveResult.Failure
import ch.abwesend.privatecontacts.domain.model.result.ContactSaveResult.Success
import ch.abwesend.privatecontacts.domain.model.result.ContactSaveResult.ValidationFailure
import ch.abwesend.privatecontacts.domain.model.result.ContactValidationError
import ch.abwesend.privatecontacts.domain.util.Constants
import ch.abwesend.privatecontacts.view.components.FullScreenError
import ch.abwesend.privatecontacts.view.components.buttons.CancelIconButton
import ch.abwesend.privatecontacts.view.components.buttons.SaveIconButton
import ch.abwesend.privatecontacts.view.components.buttons.SecondaryButton
import ch.abwesend.privatecontacts.view.components.dialogs.OkDialog
import ch.abwesend.privatecontacts.view.components.dialogs.YesNoDialog
import ch.abwesend.privatecontacts.view.model.config.ButtonConfig
import ch.abwesend.privatecontacts.view.model.screencontext.IContactEditScreenContext
import ch.abwesend.privatecontacts.view.routing.Screen.ContactEdit
import ch.abwesend.privatecontacts.view.screens.BaseScreen
import ch.abwesend.privatecontacts.view.screens.contactedit.ContactEditScreenContent.ContactEditContent
import ch.abwesend.privatecontacts.view.theme.GlobalModifiers
import ch.abwesend.privatecontacts.view.util.collectWithEffect
import ch.abwesend.privatecontacts.view.viewmodel.ContactEditViewModel
import kotlin.contracts.ExperimentalContracts

@ExperimentalComposeUiApi
@ExperimentalFoundationApi
@ExperimentalMaterialApi
@ExperimentalContracts
object ContactEditScreen {
    var isScrolling: Boolean by mutableStateOf(false) // TODO remove once google issue 212091796 is fixed

    @Composable
    fun Screen(screenContext: IContactEditScreenContext) {
        val viewModel = screenContext.contactEditViewModel
        val selectedContact = viewModel.selectedContact

        var showDiscardConfirmationDialog: Boolean by remember { mutableStateOf(false) }
        var savingErrors: List<ContactChangeError> by remember { mutableStateOf(emptyList()) }
        var validationErrors: List<ContactValidationError> by remember { mutableStateOf(emptyList()) }

        viewModel.saveResult.collectWithEffect { result ->
            showDiscardConfirmationDialog = false
            onSaveResult(
                result = result,
                onSuccess = { screenContext.returnToContactDetailScreen() },
                setSavingErrors = { savingErrors = it },
                setValidationErrors = { validationErrors = it },
            )
        }

        selectedContact?.let { contact ->
            BaseScreen(
                screenContext = screenContext,
                selectedScreen = ContactEdit,
                topBar = {
                    ContactEditTopBar(
                        screenContext = screenContext,
                        contact = contact,
                        showDiscardConfirmationDialog = { showDiscardConfirmationDialog = true },
                    )
                }
            ) { padding ->
                // The actual content
                Column(modifier = Modifier.padding(padding)) {
                    ContactEditContent(
                        viewModel = screenContext.contactEditViewModel,
                        contact = contact,
                        modifier = Modifier.weight(1F)
                    )

                    if (screenContext.settings.showExtraButtonsInEditScreen) {
                        ButtonFooter(screenContext, contact) { showDiscardConfirmationDialog = true }
                    }
                }

                // Dialogs
                DiscardConfirmationDialog(screenContext, showDiscardConfirmationDialog) {
                    showDiscardConfirmationDialog = false
                }
                SavingErrorDialog(
                    errors = savingErrors,
                    onTryAgain = { onSave(viewModel, contact) },
                    onChangeToNewContact = {
                        contact.isNew = true
                        viewModel.changeContact(contact)
                    },
                    onClose = { savingErrors = emptyList() },
                )
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
        screenContext: IContactEditScreenContext,
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
                onClick = { onSave(screenContext.contactEditViewModel, contact) },
                modifier = buttonModifier.weight(1F),
            ) {
                Text(text = stringResource(id = R.string.save))
            }
        }
    }

    @Composable
    private fun ContactEditTopBar(
        screenContext: IContactEditScreenContext,
        contact: IContactEditable,
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
                SaveIconButton { onSave(screenContext.contactEditViewModel, contact) }
            }
        )
    }

    private fun onSave(viewModel: ContactEditViewModel, contact: IContactEditable) {
        viewModel.saveContact(contact)
    }

    private fun onSaveResult(
        result: ContactSaveResult,
        onSuccess: () -> Unit,
        setValidationErrors: (List<ContactValidationError>) -> Unit,
        setSavingErrors: (List<ContactChangeError>) -> Unit,
    ) {
        when (result) {
            is Success -> onSuccess()
            is Failure -> setSavingErrors(result.errors)
            is ValidationFailure -> setValidationErrors(result.validationErrors)
        }
    }

    private fun onDiscard(
        screenContext: IContactEditScreenContext,
        showConfirmationDialog: () -> Unit,
    ) {
        if (hasChanges(screenContext.contactEditViewModel)) {
            showConfirmationDialog()
        } else {
            onDiscardConfirmed(screenContext)
        }
    }

    private fun onDiscardConfirmed(screenContext: IContactEditScreenContext) {
        screenContext.navigateUp()
        screenContext.contactEditViewModel.clearContact()
    }

    private fun hasChanges(viewModel: ContactEditViewModel): Boolean =
        viewModel.let {
            it.originalContact != it.selectedContact?.contact
        }

    @Composable
    private fun DiscardConfirmationDialog(
        screenContext: IContactEditScreenContext,
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
        errors: List<ContactChangeError>,
        onClose: () -> Unit,
        onTryAgain: () -> Unit,
        onChangeToNewContact: () -> Unit,
    ) {
        if (errors.isNotEmpty()) {
            OkDialog(title = R.string.error, onClose = onClose, okButtonLabel = R.string.close) {
                val description = if (errors.size == 1) {
                    val errorText = stringResource(id = errors.first().label)
                    stringResource(R.string.saving_data_error, errorText)
                } else {
                    val partialTexts = errors.map { stringResource(id = it.label) }
                    val errorText = partialTexts.joinToString(separator = Constants.doubleLinebreak)
                    stringResource(R.string.saving_data_error, errorText)
                }

                Column {
                    Text(text = description)

                    if (errors.contains(UNABLE_TO_RESOLVE_EXISTING_CONTACT)) {
                        UnableToResolveExistingContactExplanation(
                            onClose = onClose,
                            onTryAgain = onTryAgain,
                            onChangeToNewContact = onChangeToNewContact,
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun UnableToResolveExistingContactExplanation(
        onClose: () -> Unit,
        onTryAgain: () -> Unit,
        onChangeToNewContact: () -> Unit,
    ) {
        Spacer(modifier = Modifier.height(10.dp))
        Divider()
        Spacer(modifier = Modifier.height(10.dp))
        Text(text = stringResource(id = R.string.saving_data_error_change_to_new_contact))
        Spacer(modifier = Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            SecondaryButton(
                modifier = Modifier.padding(end = 20.dp),
                onClick = {
                    onTryAgain()
                    onClose()
                },
                content = { Text(text = stringResource(id = R.string.try_again)) }
            )
            SecondaryButton(
                onClick = {
                    onChangeToNewContact()
                    onClose()
                },
                content = { Text(text = stringResource(id = R.string.create_as_new_contact)) }
            )
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
