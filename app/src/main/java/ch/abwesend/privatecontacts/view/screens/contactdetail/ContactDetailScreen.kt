/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.screens.contactdetail

import androidx.annotation.StringRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Sync
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.lib.flow.AsyncResource
import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.model.contact.IContact
import ch.abwesend.privatecontacts.domain.model.contact.asEditable
import ch.abwesend.privatecontacts.domain.model.result.ContactChangeError
import ch.abwesend.privatecontacts.domain.model.result.ContactDeleteResult
import ch.abwesend.privatecontacts.domain.model.result.ContactSaveResult
import ch.abwesend.privatecontacts.domain.model.result.ContactValidationError
import ch.abwesend.privatecontacts.domain.model.result.generic.ErrorResult
import ch.abwesend.privatecontacts.domain.model.result.generic.SuccessResult
import ch.abwesend.privatecontacts.view.components.FullScreenError
import ch.abwesend.privatecontacts.view.components.LoadingIndicatorFullScreen
import ch.abwesend.privatecontacts.view.components.buttons.BackIconButton
import ch.abwesend.privatecontacts.view.components.buttons.EditIconButton
import ch.abwesend.privatecontacts.view.components.buttons.MoreActionsIconButton
import ch.abwesend.privatecontacts.view.components.contactmenu.ChangeContactTypeErrorDialog
import ch.abwesend.privatecontacts.view.components.contactmenu.ChangeContactTypeMenuItem
import ch.abwesend.privatecontacts.view.components.contactmenu.DeleteContactMenuItem
import ch.abwesend.privatecontacts.view.components.contactmenu.DeleteContactsResultDialog
import ch.abwesend.privatecontacts.view.components.contactmenu.ExportContactsMenuItem
import ch.abwesend.privatecontacts.view.components.contactmenu.ExportContactsResultDialog
import ch.abwesend.privatecontacts.view.model.ContactTypeChangeMenuConfig
import ch.abwesend.privatecontacts.view.model.config.ButtonConfig
import ch.abwesend.privatecontacts.view.model.screencontext.IContactDetailScreenContext
import ch.abwesend.privatecontacts.view.routing.Screen
import ch.abwesend.privatecontacts.view.screens.BaseScreen
import ch.abwesend.privatecontacts.view.util.collectWithEffect
import ch.abwesend.privatecontacts.view.util.composeIfError
import ch.abwesend.privatecontacts.view.util.composeIfInactive
import ch.abwesend.privatecontacts.view.util.composeIfLoading
import ch.abwesend.privatecontacts.view.util.composeIfReady
import ch.abwesend.privatecontacts.view.viewmodel.ContactDetailViewModel
import kotlinx.coroutines.FlowPreview
import kotlin.contracts.ExperimentalContracts

@ExperimentalFoundationApi
@ExperimentalMaterialApi
@ExperimentalComposeUiApi
@FlowPreview
@ExperimentalContracts
object ContactDetailScreen {

    @Composable
    fun Screen(screenContext: IContactDetailScreenContext) {
        val viewModel = screenContext.contactDetailViewModel
        val contactResource: AsyncResource<IContact> by viewModel.selectedContact.collectAsState()

        BaseScreen(
            screenContext = screenContext,
            selectedScreen = Screen.ContactDetail,
            topBar = {
                ContactDetailTopBar(screenContext = screenContext, contact = contactResource.valueOrNull)
            }
        ) { padding ->
            val modifier = Modifier.padding(padding)
            contactResource
                .composeIfError { NoContactLoadedError(viewModel = viewModel, modifier = modifier) }
                .composeIfInactive { NoContactLoaded(modifier = modifier, navigateUp = screenContext::navigateUp) }
                .composeIfLoading {
                    LoadingIndicatorFullScreen(textAfterIndicator = R.string.loading_contacts, modifier = modifier)
                }
                .composeIfReady { ContactDetailScreenContent.ScreenContent(contact = it, modifier = modifier) }
        }

        DeleteResultObserver(viewModel = viewModel, onSuccess = screenContext::navigateUp)
        TypeChangeResultObserver(viewModel = viewModel, onSuccess = screenContext::navigateUp)
        ExportResultObserver(viewModel)
    }

    @Composable
    private fun DeleteResultObserver(viewModel: ContactDetailViewModel, onSuccess: () -> Unit) {
        var deletionErrors: List<ContactChangeError> by remember { mutableStateOf(emptyList()) }

        DeleteContactsResultDialog(numberOfErrors = deletionErrors.size, numberOfAttemptedChanges = 1) {
            deletionErrors = emptyList()
        }

        viewModel.deleteResult.collectWithEffect { result ->
            when (result) {
                is ContactDeleteResult.Success -> onSuccess()
                is ContactDeleteResult.Failure -> deletionErrors = result.errors
            }
        }
    }

    @Composable
    private fun TypeChangeResultObserver(viewModel: ContactDetailViewModel, onSuccess: () -> Unit) {
        var validationErrors: List<ContactValidationError> by remember { mutableStateOf(emptyList()) }
        var errors: List<ContactChangeError> by remember { mutableStateOf(emptyList()) }
        val changeSuccessful = validationErrors.isEmpty() && errors.isEmpty()

        ChangeContactTypeErrorDialog(
            validationErrors = validationErrors,
            errors = errors,
            numberOfAttemptedChanges = 1,
            numberOfSuccessfulChanges = if (changeSuccessful) 1 else 0
        ) {
            validationErrors = emptyList()
            errors = emptyList()
        }

        viewModel.typeChangeResult.collectWithEffect { result ->
            when (result) {
                is ContactSaveResult.Success -> onSuccess()
                is ContactSaveResult.ValidationFailure -> validationErrors = result.validationErrors
                is ContactSaveResult.Failure -> errors = result.errors
            }
        }
    }

    @Composable
    private fun ExportResultObserver(viewModel: ContactDetailViewModel) {
        var numberOfExportErrors: Int by remember { mutableIntStateOf(0) }
        var dialogVisible: Boolean by remember { mutableStateOf(false) }

        if (dialogVisible) {
            ExportContactsResultDialog(numberOfErrors = numberOfExportErrors, numberOfAttemptedChanges = 1) {
                dialogVisible = false
            }
        }

        viewModel.exportResult.collectWithEffect { result ->
            numberOfExportErrors = when (result) {
                is SuccessResult -> result.value.failedContacts.size
                is ErrorResult -> 1
            }
            dialogVisible = true
        }
    }

    @Composable
    private fun ContactDetailTopBar(
        screenContext: IContactDetailScreenContext,
        contact: IContact?,
    ) {
        @StringRes val title = R.string.screen_contact_details

        var dropDownMenuExpanded: Boolean by remember { mutableStateOf(false) }

        TopAppBar(
            title = {
                Text(
                    text = contact?.displayName ?: stringResource(id = title),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            navigationIcon = {
                BackIconButton { screenContext.navigateUp() }
            },
            actions = {
                if (contact != null) {
                    EditIconButton {
                        screenContext.navigateToContactEditScreen(contact)
                    }
                    MoreActionsIconButton {
                        dropDownMenuExpanded = true
                    }
                    ActionsMenu(
                        viewModel = screenContext.contactDetailViewModel,
                        contact = contact,
                        expanded = dropDownMenuExpanded
                    ) { dropDownMenuExpanded = false }
                }
            }
        )
    }

    @Composable
    fun ActionsMenu(
        viewModel: ContactDetailViewModel,
        contact: IContact,
        expanded: Boolean,
        onCloseMenu: () -> Unit,
    ) {
        val hasWritePermission = remember { viewModel.hasContactWritePermission }
        DropdownMenu(expanded = expanded, onDismissRequest = onCloseMenu) {
            DropdownMenuItem(
                onClick = {
                    viewModel.reloadContact(contact)
                    onCloseMenu()
                },
                content = { Text(stringResource(id = R.string.refresh)) }
            )
            Divider()
            ContactType.entries.forEach { targetType ->
                ChangeContactTypeMenuItem(
                    viewModel = viewModel,
                    contact = contact,
                    targetType = targetType,
                    enabled = hasWritePermission || !targetType.androidPermissionRequired,
                    onCloseMenu = onCloseMenu,
                )
            }
            DeleteMenuItem(viewModel, contact, onCloseMenu)
            Divider()
            ExportMenuItem(viewModel, contact, onCloseMenu)
        }
    }

    @Composable
    private fun ChangeContactTypeMenuItem(
        viewModel: ContactDetailViewModel,
        contact: IContact,
        targetType: ContactType,
        enabled: Boolean,
        onCloseMenu: () -> Unit,
    ) {
        if (contact.type != targetType) {
            val config = ContactTypeChangeMenuConfig.fromTargetType(targetType)
            val editableContact = contact.asEditable()
            ChangeContactTypeMenuItem(
                contacts = setOf(editableContact),
                config = config,
                enabled = enabled
            ) { changeContact ->
                if (changeContact) {
                    viewModel.changeContactType(editableContact, targetType)
                }
                onCloseMenu()
            }
        }
    }

    @Composable
    private fun DeleteMenuItem(
        viewModel: ContactDetailViewModel,
        contact: IContact,
        onCloseMenu: () -> Unit,
    ) {
        DeleteContactMenuItem(numberOfContacts = 1) { delete ->
            if (delete) {
                viewModel.deleteContact(contact)
            }
            onCloseMenu()
        }
    }

    @Composable
    private fun NoContactLoaded(modifier: Modifier = Modifier, navigateUp: () -> Unit) {
        FullScreenError(
            errorMessage = R.string.no_contact_selected,
            modifier = modifier,
            buttonConfig = ButtonConfig(
                label = R.string.back,
                icon = Icons.Default.ArrowBack,
                onClick = navigateUp
            )
        )
    }

    @Composable
    private fun NoContactLoadedError(viewModel: ContactDetailViewModel, modifier: Modifier = Modifier) {
        FullScreenError(
            errorMessage = R.string.no_contact_selected_error,
            modifier = modifier,
            buttonConfig = ButtonConfig(
                label = R.string.reload_contact,
                icon = Icons.Default.Sync,
            ) {
                viewModel.reloadContact()
            }
        )
    }

    @Composable
    private fun ExportMenuItem(
        viewModel: ContactDetailViewModel,
        contact: IContact,
        onCloseMenu: () -> Unit,
    ) {
        ExportContactsMenuItem(
            contacts = setOf(contact),
            onCancel = onCloseMenu,
            onExportContact = { targetFile, vCardVersion ->
                viewModel.exportContact(targetFile, vCardVersion, contact)
                onCloseMenu()
            }
        )
    }
}
