/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.screens.contactdetail

import androidx.annotation.StringRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material.DropdownMenu
import androidx.compose.material.ExperimentalMaterialApi
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.lib.flow.AsyncResource
import ch.abwesend.privatecontacts.domain.model.contact.ContactType.PUBLIC
import ch.abwesend.privatecontacts.domain.model.contact.ContactType.SECRET
import ch.abwesend.privatecontacts.domain.model.contact.IContact
import ch.abwesend.privatecontacts.domain.model.result.ContactChangeError
import ch.abwesend.privatecontacts.domain.model.result.ContactDeleteResult
import ch.abwesend.privatecontacts.domain.model.result.ContactSaveResult
import ch.abwesend.privatecontacts.domain.model.result.ContactValidationError
import ch.abwesend.privatecontacts.view.components.FullScreenError
import ch.abwesend.privatecontacts.view.components.LoadingIndicatorFullScreen
import ch.abwesend.privatecontacts.view.components.buttons.BackIconButton
import ch.abwesend.privatecontacts.view.components.buttons.EditIconButton
import ch.abwesend.privatecontacts.view.components.buttons.MoreActionsIconButton
import ch.abwesend.privatecontacts.view.components.contactmenu.ChangeContactTypeErrorDialog
import ch.abwesend.privatecontacts.view.components.contactmenu.DeleteContactMenuItem
import ch.abwesend.privatecontacts.view.components.contactmenu.DeleteContactsResultDialog
import ch.abwesend.privatecontacts.view.components.contactmenu.MakeContactSecretMenuItem
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

@ExperimentalFoundationApi
@ExperimentalMaterialApi
@ExperimentalComposeUiApi
@FlowPreview
object ContactDetailScreen {

    @Composable
    fun Screen(screenContext: ScreenContext) {
        val viewModel = screenContext.contactDetailViewModel
        val contactResource: AsyncResource<IContact> by viewModel.selectedContact.collectAsState()

        Scaffold(
            topBar = {
                ContactDetailTopBar(
                    screenContext = screenContext,
                    contact = contactResource.valueOrNull
                )
            }
        ) { padding ->
            val modifier = Modifier.padding(padding)
            contactResource
                .composeIfError { NoContactLoadedError(viewModel = viewModel, modifier = modifier) }
                .composeIfInactive { NoContactLoaded(router = screenContext.router, modifier = modifier) }
                .composeIfLoading {
                    LoadingIndicatorFullScreen(textAfterIndicator = R.string.loading_contacts, modifier = modifier)
                }
                .composeIfReady { ContactDetailScreenContent.ScreenContent(contact = it, modifier = modifier) }
        }

        DeleteResultObserver(viewModel, screenContext.router)
        TypeChangeResultObserver(viewModel, screenContext.router)
    }

    @Composable
    private fun DeleteResultObserver(viewModel: ContactDetailViewModel, router: AppRouter) {
        var deletionErrors: List<ContactChangeError> by remember { mutableStateOf(emptyList()) }

        DeleteContactsResultDialog(numberOfErrors = deletionErrors.size, numberOfAttemptedChanges = 1) {
            deletionErrors = emptyList()
        }

        LaunchedEffect(Unit) {
            viewModel.deleteResult.collect { result ->
                when (result) {
                    is ContactDeleteResult.Success -> router.navigateUp()
                    is ContactDeleteResult.Failure -> deletionErrors = result.errors
                }
            }
        }
    }

    @Composable
    private fun TypeChangeResultObserver(viewModel: ContactDetailViewModel, router: AppRouter) {
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

        LaunchedEffect(Unit) {
            viewModel.typeChangeResult.collect { result ->
                when (result) {
                    is ContactSaveResult.Success -> router.navigateUp()
                    is ContactSaveResult.ValidationFailure -> validationErrors = result.validationErrors
                    is ContactSaveResult.Failure -> errors = result.errors
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
        onCloseMenu: () -> Unit,
    ) {
        DropdownMenu(expanded = expanded, onDismissRequest = onCloseMenu) {
            MakeContactSecretMenuItem(screenContext.contactDetailViewModel, contact, onCloseMenu)
            DeleteMenuItem(screenContext, contact, onCloseMenu)
        }
    }

    @Composable
    private fun MakeContactSecretMenuItem(
        viewModel: ContactDetailViewModel,
        contact: IContact,
        onCloseMenu: () -> Unit,
    ) {
        when (contact.type) {
            SECRET -> Unit
            PUBLIC -> {
                MakeContactSecretMenuItem(contacts = setOf(contact)) { changeContact ->
                    if (changeContact) {
                        viewModel.changeContactType(contact, SECRET)
                    }
                    onCloseMenu()
                }
            }
        }
    }

    @Composable
    private fun DeleteMenuItem(
        screenContext: ScreenContext,
        contact: IContact,
        onCloseMenu: () -> Unit,
    ) {
        DeleteContactMenuItem(contacts = setOf(contact)) { delete ->
            if (delete) {
                screenContext.contactDetailViewModel.deleteContact(contact)
            }
            onCloseMenu()
        }
    }

    @Composable
    private fun NoContactLoaded(router: AppRouter, modifier: Modifier = Modifier) {
        FullScreenError(
            errorMessage = R.string.no_contact_selected,
            modifier = modifier,
            buttonConfig = ButtonConfig(
                label = R.string.back,
                icon = Icons.Default.ArrowBack,
            ) {
                router.navigateUp()
            }
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
}
