/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.screens.contactdetail

import androidx.annotation.StringRes
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Sync
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.lib.flow.AsyncResource
import ch.abwesend.privatecontacts.domain.model.contact.IContact
import ch.abwesend.privatecontacts.domain.model.contact.getFullName
import ch.abwesend.privatecontacts.domain.model.result.ContactChangeError
import ch.abwesend.privatecontacts.domain.model.result.ContactDeleteResult
import ch.abwesend.privatecontacts.view.components.FullScreenError
import ch.abwesend.privatecontacts.view.components.LoadingIndicatorFullScreen
import ch.abwesend.privatecontacts.view.components.buttons.BackIconButton
import ch.abwesend.privatecontacts.view.components.buttons.EditIconButton
import ch.abwesend.privatecontacts.view.components.buttons.MoreActionsIconButton
import ch.abwesend.privatecontacts.view.components.dialogs.OkDialog
import ch.abwesend.privatecontacts.view.components.dialogs.YesNoDialog
import ch.abwesend.privatecontacts.view.model.ScreenContext
import ch.abwesend.privatecontacts.view.model.config.ButtonConfig
import ch.abwesend.privatecontacts.view.routing.AppRouter
import ch.abwesend.privatecontacts.view.routing.Screen
import ch.abwesend.privatecontacts.view.util.composeIfError
import ch.abwesend.privatecontacts.view.util.composeIfInactive
import ch.abwesend.privatecontacts.view.util.composeIfLoading
import ch.abwesend.privatecontacts.view.util.composeIfReady
import ch.abwesend.privatecontacts.view.viewmodel.ContactDetailViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collect

@ExperimentalComposeUiApi
@FlowPreview
object ContactDetailScreen {

    @Composable
    fun Screen(screenContext: ScreenContext) {
        val viewModel = screenContext.contactDetailViewModel
        val contactResource: AsyncResource<IContact> by viewModel.selectedContact.collectAsState()
        var deletionError: ContactChangeError? by remember { mutableStateOf(null) }

        Scaffold(
            topBar = {
                ContactDetailTopBar(
                    screenContext = screenContext,
                    contact = contactResource.valueOrNull
                )
            }
        ) {
            contactResource
                .composeIfError { NoContactLoadedError(viewModel = viewModel) }
                .composeIfInactive { NoContactLoaded(router = screenContext.router) }
                .composeIfLoading { LoadingIndicatorFullScreen(R.string.loading_contacts) }
                .composeIfReady { ContactDetailScreenContent.ScreenContent(contact = it) }
        }

        DeleteErrorDialog(error = deletionError) {
            deletionError = null
        }

        LaunchedEffect(Unit) {
            viewModel.deleteResult.collect { result ->
                when (result) {
                    is ContactDeleteResult.Success -> screenContext.router.navigateUp()
                    is ContactDeleteResult.Failure -> deletionError = result.error
                }
            }
        }
    }

    @Composable
    private fun ContactDetailTopBar(
        screenContext: ScreenContext,
        contact: IContact?,
    ) {
        val buttonsEnabled = contact != null
        @StringRes val title = R.string.screen_contact_details

        var dropDownMenuExpanded: Boolean by remember { mutableStateOf(false)}

        TopAppBar(
            title = {
                Text(
                    text = contact?.getFullName() ?: stringResource(id = title),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            navigationIcon = {
                BackIconButton { screenContext.router.navigateUp() }
            },
            actions = {
                if (contact != null) {
                    EditIconButton(enabled = buttonsEnabled) {
                        screenContext.contactEditViewModel.selectContact(contact)
                        screenContext.router.navigateToScreen(Screen.ContactEdit)
                    }
                    MoreActionsIconButton(enabled = buttonsEnabled) {
                        dropDownMenuExpanded = true
                    }
                    ActionsMenu(screenContext = screenContext, contact = contact, expanded = dropDownMenuExpanded) {
                        dropDownMenuExpanded = false
                    }
                }
            }
        )
    }

    @Composable
    fun ActionsMenu(
        screenContext: ScreenContext,
        contact: IContact,
        expanded: Boolean,
        onCloseMenu: () -> Unit
    ) {
        var deleteConfirmationDialogVisible: Boolean by remember { mutableStateOf(false) }

        DropdownMenu(expanded = expanded, onDismissRequest = onCloseMenu) {
            DropdownMenuItem(onClick = { deleteConfirmationDialogVisible = true }) {
                Text(stringResource(id = R.string.delete_contact))
            }
        }
        DeleteConfirmationDialog(
            screenContext = screenContext,
            contact = contact,
            visible = deleteConfirmationDialogVisible,
            hideDialog = {
                deleteConfirmationDialogVisible = false
                onCloseMenu()
            },
        )
    }

    @Composable
    private fun DeleteConfirmationDialog(
        screenContext: ScreenContext,
        contact: IContact,
        visible: Boolean,
        hideDialog: () -> Unit,
    ) {
        if (visible) {
            YesNoDialog(
                title = R.string.delete_contact_title,
                text = R.string.delete_contact_text,
                onYes = {
                    hideDialog()
                    screenContext.contactDetailViewModel.deleteContact(contact)
                },
                onNo = hideDialog
            )
        }
    }

    @Composable
    private fun NoContactLoaded(router: AppRouter) {
        FullScreenError(
            errorMessage = R.string.no_contact_selected,
            buttonConfig = ButtonConfig(
                label = R.string.back,
                icon = Icons.Default.ArrowBack,
            ) {
                router.navigateUp()
            }
        )
    }

    @Composable
    private fun NoContactLoadedError(viewModel: ContactDetailViewModel) {
        FullScreenError(
            errorMessage = R.string.no_contact_selected_error,
            buttonConfig = ButtonConfig(
                label = R.string.reload_contact,
                icon = Icons.Default.Sync,
            ) {
                viewModel.reloadContact()
            }
        )
    }

    @Composable
    private fun DeleteErrorDialog(
        error: ContactChangeError?,
        onClose: () -> Unit
    ) {
        error?.let { savingError ->
            OkDialog(
                title = R.string.error,
                onClose = onClose
            ) {
                val errorText = stringResource(id = savingError.label)
                val description = stringResource(R.string.delete_contact_error, errorText)
                Text(text = description)
            }
        }
    }
}
