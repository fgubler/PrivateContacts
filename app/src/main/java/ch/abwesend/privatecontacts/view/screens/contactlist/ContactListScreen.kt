/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.screens.contactlist

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.LeadingIconTab
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.res.stringResource
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.lib.flow.ErrorResource
import ch.abwesend.privatecontacts.domain.lib.flow.InactiveResource
import ch.abwesend.privatecontacts.domain.lib.flow.LoadingResource
import ch.abwesend.privatecontacts.domain.lib.flow.ReadyResource
import ch.abwesend.privatecontacts.domain.model.contact.IContactBase
import ch.abwesend.privatecontacts.domain.model.contact.isExternal
import ch.abwesend.privatecontacts.domain.model.result.ContactDeleteResult
import ch.abwesend.privatecontacts.domain.service.interfaces.PermissionService
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import ch.abwesend.privatecontacts.view.components.FullScreenError
import ch.abwesend.privatecontacts.view.components.LoadingIndicatorFullScreen
import ch.abwesend.privatecontacts.view.components.contact.DeleteContactsErrorDialog
import ch.abwesend.privatecontacts.view.model.ContactListScreenState
import ch.abwesend.privatecontacts.view.model.ScreenContext
import ch.abwesend.privatecontacts.view.model.config.ButtonConfig
import ch.abwesend.privatecontacts.view.routing.Screen
import ch.abwesend.privatecontacts.view.screens.BaseScreen
import ch.abwesend.privatecontacts.view.viewmodel.ContactListViewModel
import kotlinx.coroutines.FlowPreview

@ExperimentalFoundationApi
@ExperimentalComposeUiApi
@FlowPreview
object ContactListScreen {
    private val permissionService: PermissionService by injectAnywhere()

    @Composable
    fun Screen(screenContext: ScreenContext) {
        val scaffoldState = rememberScaffoldState()

        LaunchedEffect(Unit) {
            screenContext.contactListViewModel.reloadContacts()
        }

        BaseScreen(
            screenContext = screenContext,
            selectedScreen = Screen.ContactList,
            allowFullNavigation = true,
            scaffoldState = scaffoldState,
            topBar = {
                ContactListTopBar(
                    viewModel = screenContext.contactListViewModel,
                    scaffoldState = scaffoldState,
                )
            },
            floatingActionButton = { AddContactButton(screenContext) }
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                TabBox(screenContext)
                ContactListContent(screenContext)
            }
        }
    }

    @Composable
    private fun TabBox(screenContext: ScreenContext) {
        val hasContactsPermission = permissionService.hasContactReadPermission()
        val androidContactsEnabled = screenContext.settings.showAndroidContacts

        if (androidContactsEnabled && hasContactsPermission) {
            val viewModel = remember { screenContext.contactListViewModel }
            val selectedTab = viewModel.selectedTab.value

            TabRow(selectedTabIndex = selectedTab.index, backgroundColor = MaterialTheme.colors.surface) {
                ContactListTab.valuesSorted.forEach { tab ->
                    LeadingIconTab(
                        selected = selectedTab == tab,
                        onClick = { viewModel.selectTab(tab) },
                        text = { Text(text = stringResource(id = tab.label)) },
                        icon = { Icon(imageVector = tab.icon, contentDescription = stringResource(id = tab.label)) }
                    )
                }
            }
        }
    }

    @Composable
    private fun AddContactButton(screenContext: ScreenContext) {
        FloatingActionButton(onClick = { createContact(screenContext) }) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(id = R.string.create_contact),
                tint = MaterialTheme.colors.onSecondary,
            )
        }
    }

    @Composable
    private fun ContactListContent(screenContext: ScreenContext) {
        val viewModel = screenContext.contactListViewModel
        val contactsResource = viewModel.contacts.collectAsState(initial = InactiveResource()).value

        val screenState = viewModel.screenState.value
        val bulkMode = screenState is ContactListScreenState.BulkMode
        val selectedContacts = (screenState as? ContactListScreenState.BulkMode)
            ?.selectedContacts.orEmpty()

        val showContactList: @Composable (contacts: List<IContactBase>) -> Unit = { contacts ->
            ContactList(
                contacts = contacts,
                selectedContacts = selectedContacts,
                onContactClicked = { contact -> selectContact(screenContext, contact, bulkMode) },
                onContactLongClicked = { contact -> longClickContact(screenContext, contact) }
            )
        }

        when (contactsResource) {
            is ErrorResource -> LoadingError(viewModel)
            is LoadingResource -> ContactLoadingIndicator()
            is InactiveResource -> showContactList(emptyList())
            is ReadyResource -> showContactList(contactsResource.value)
        }

        val deletionErrors = viewModel.deleteResult
            .collectAsState(initial = ContactDeleteResult.Inactive)
            .let { it.value as? ContactDeleteResult.Failure }
            ?.errors.orEmpty()

        DeleteContactsErrorDialog(errors = deletionErrors, multipleContacts = selectedContacts.size > 1) {
            viewModel.resetDeletionResult()
        }
    }

    @Composable
    private fun ContactLoadingIndicator() = LoadingIndicatorFullScreen(R.string.loading_contacts)

    @Composable
    private fun LoadingError(viewModel: ContactListViewModel) {
        FullScreenError(
            errorMessage = R.string.data_loading_error,
            buttonConfig = ButtonConfig(
                label = R.string.reload_data,
                icon = Icons.Default.Sync
            ) { viewModel.reloadContacts() },
        )
    }

    private fun selectContact(screenContext: ScreenContext, contact: IContactBase, bulkMode: Boolean) {
        if (bulkMode) {
            screenContext.contactListViewModel.toggleContactSelected(contact)
        } else {
            if (!contact.isExternal) {
                // TODO implement opening external contacts
                screenContext.contactDetailViewModel.selectContact(contact)
            }
            screenContext.router.navigateToScreen(Screen.ContactDetail)
        }
    }

    private fun longClickContact(screenContext: ScreenContext, contact: IContactBase) {
        screenContext.contactListViewModel.setBulkMode(enabled = true)
        selectContact(screenContext, contact, bulkMode = true)
    }

    private fun createContact(screenContext: ScreenContext) {
        screenContext.contactEditViewModel.createContact()
        screenContext.router.navigateToScreen(Screen.ContactEdit)
    }
}
