/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.screens.contactlist

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.LeadingIconTab
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import ch.abwesend.privatecontacts.domain.lib.flow.ErrorResource
import ch.abwesend.privatecontacts.domain.lib.flow.InactiveResource
import ch.abwesend.privatecontacts.domain.lib.flow.LoadingResource
import ch.abwesend.privatecontacts.domain.lib.flow.ReadyResource
import ch.abwesend.privatecontacts.domain.model.contact.ContactId
import ch.abwesend.privatecontacts.domain.model.contact.IContactBase
import ch.abwesend.privatecontacts.domain.model.result.batch.flattenedErrors
import ch.abwesend.privatecontacts.domain.settings.ISettingsState
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import ch.abwesend.privatecontacts.view.components.FullScreenError
import ch.abwesend.privatecontacts.view.components.LoadingIndicatorFullScreen
import ch.abwesend.privatecontacts.view.components.contactmenu.ChangeContactTypeErrorDialog
import ch.abwesend.privatecontacts.view.components.contactmenu.ChangeContactTypeLoadingDialog
import ch.abwesend.privatecontacts.view.components.contactmenu.DeleteContactsLoadingDialog
import ch.abwesend.privatecontacts.view.components.contactmenu.DeleteContactsResultDialog
import ch.abwesend.privatecontacts.view.components.dialogs.ResourceFlowProgressAndResultDialog
import ch.abwesend.privatecontacts.view.model.ContactListScreenState
import ch.abwesend.privatecontacts.view.model.config.ButtonConfig
import ch.abwesend.privatecontacts.view.model.screencontext.IContactListScreenContext
import ch.abwesend.privatecontacts.view.permission.AndroidContactPermissionHelper
import ch.abwesend.privatecontacts.view.routing.Screen
import ch.abwesend.privatecontacts.view.screens.BaseScreen
import ch.abwesend.privatecontacts.view.screens.contactlist.ContactListTab.ALL_CONTACTS
import ch.abwesend.privatecontacts.view.screens.contactlist.ContactListTab.SECRET_CONTACTS
import ch.abwesend.privatecontacts.view.viewmodel.ContactListViewModel
import kotlinx.coroutines.FlowPreview
import kotlin.contracts.ExperimentalContracts

@ExperimentalMaterialApi
@ExperimentalFoundationApi
@ExperimentalComposeUiApi
@FlowPreview
@ExperimentalContracts
object ContactListScreen {
    private val contactPermissionHelper: AndroidContactPermissionHelper by injectAnywhere()

    @Composable
    fun Screen(screenContext: IContactListScreenContext) {
        val scaffoldState = rememberScaffoldState()
        val viewModel = screenContext.contactListViewModel
        val settings = screenContext.settings

        LaunchedEffect(Unit) { initializeScreen(viewModel, settings) }

        BaseScreen(
            screenContext = screenContext,
            selectedScreen = Screen.ContactList,
            allowFullNavigation = true,
            scaffoldState = scaffoldState,
            topBar = {
                ContactListTopBar(
                    viewModel = viewModel,
                    scaffoldState = scaffoldState,
                )
            },
            floatingActionButton = { AddContactButton(screenContext) }
        ) { padding ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(padding)
            ) {
                if (settings.invertTopAndBottomBars) {
                    Surface(modifier = Modifier.weight(1.0f)) { // let the tabs get their space first
                        ContactListContent(screenContext)
                    }
                    TabBox(viewModel, settings)
                } else {
                    TabBox(viewModel, settings)
                    ContactListContent(screenContext)
                }
            }
        }
    }

    private fun initializeScreen(viewModel: ContactListViewModel, settings: ISettingsState) {
        viewModel.reloadContacts()

        when (viewModel.selectedTab.value) {
            SECRET_CONTACTS -> { /* nothing to do */ }
            ALL_CONTACTS -> if (!settings.showAndroidContacts) {
                viewModel.selectTab(ContactListTab.default)
            }
        }
    }

    @Composable
    private fun TabBox(viewModel: ContactListViewModel, settings: ISettingsState) {
        val androidContactsEnabled = settings.showAndroidContacts

        if (androidContactsEnabled) {
            val selectedTab = viewModel.selectedTab.value

            TabRow(selectedTabIndex = selectedTab.index, backgroundColor = MaterialTheme.colors.surface) {
                ContactListTab.valuesSorted.forEach { tab ->
                    Tab(tab = tab, selectedTab = selectedTab, viewModel = viewModel)
                }
            }
        }
    }

    @Composable
    private fun Tab(tab: ContactListTab, selectedTab: ContactListTab, viewModel: ContactListViewModel) {
        var requestPermissions: Boolean by remember { mutableStateOf(false) }

        LeadingIconTab(
            selected = selectedTab == tab,
            text = { Text(text = stringResource(id = tab.label)) },
            icon = { Icon(imageVector = tab.icon, contentDescription = stringResource(id = tab.label)) },
            onClick = {
                if (tab.requiresPermission) {
                    requestPermissions = true
                } else {
                    viewModel.selectTab(tab)
                }
            },
        )

        if (requestPermissions) {
            contactPermissionHelper.requestAndroidContactPermissions {
                requestPermissions = false
                if (it.usable) {
                    viewModel.selectTab(tab)
                }
            }
        }
    }

    @Composable
    private fun AddContactButton(screenContext: IContactListScreenContext) {
        val verticalOffset = if (screenContext.settings.invertTopAndBottomBars) -45 else 0 // space for tab-headers
        FloatingActionButton(
            modifier = Modifier.offset(y = verticalOffset.dp),
            onClick = { createContact(screenContext) },
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(id = R.string.create_contact),
                tint = MaterialTheme.colors.onSecondary,
            )
        }
    }

    @Composable
    private fun ContactListContent(screenContext: IContactListScreenContext) {
        val viewModel = screenContext.contactListViewModel
        val contactsResource = viewModel.contacts.collectAsState(initial = InactiveResource()).value

        val screenState = viewModel.screenState.value
        val bulkMode = screenState is ContactListScreenState.BulkMode
        val selectedContacts = (screenState as? ContactListScreenState.BulkMode)
            ?.selectedContacts.orEmpty().map { it.id }.toSet()

        val showTypeIcons = viewModel.selectedTab.value.showContactTypeIcons &&
            screenContext.settings.showContactTypeInList

        val showContactList: @Composable (contacts: List<IContactBase>) -> Unit = { contacts ->
            ContactList(
                contacts = contacts,
                selectedContacts = selectedContacts,
                scrollingState = viewModel.scrollingState,
                showTypeIcons = showTypeIcons,
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

        TypeChangeResultHandler(viewModel, selectedContacts)
        DeletionResultHandler(viewModel, selectedContacts)
    }

    @Composable
    private fun DeletionResultHandler(viewModel: ContactListViewModel, selectedContacts: Set<ContactId>) {
        ResourceFlowProgressAndResultDialog(
            flow = viewModel.deleteResult,
            onCloseDialog = { viewModel.resetDeletionResult() },
            ProgressDialog = { DeleteContactsLoadingDialog(deleteMultiple = selectedContacts.size > 1) },
            ResultDialog = { result, onClose ->
                val numberOfFailed = result.failedChanges.size
                val totalNumber = numberOfFailed + result.successfulChanges.size
                DeleteContactsResultDialog(
                    numberOfErrors = numberOfFailed,
                    numberOfAttemptedChanges = totalNumber,
                    onClose = onClose,
                )
            }
        )
    }

    @Composable
    private fun TypeChangeResultHandler(viewModel: ContactListViewModel, selectedContacts: Set<ContactId>) {
        ResourceFlowProgressAndResultDialog(
            flow = viewModel.typeChangeResult,
            onCloseDialog = { viewModel.resetTypeChangeResult() },
            ProgressDialog = { ChangeContactTypeLoadingDialog(changeMultiple = selectedContacts.size > 1) },
            ResultDialog = { result, onClose ->
                val flattenedErrors = result.flattenedErrors()
                ChangeContactTypeErrorDialog(
                    validationErrors = flattenedErrors.validationErrors,
                    errors = flattenedErrors.errors,
                    numberOfAttemptedChanges = result.numberOfAttemptedChanges,
                    numberOfSuccessfulChanges = result.successfulChanges.size,
                    onClose = onClose,
                )
            }
        )
    }

    @Composable
    private fun ContactLoadingIndicator() = LoadingIndicatorFullScreen(textAfterIndicator = R.string.loading_contacts)

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

    private fun selectContact(screenContext: IContactListScreenContext, contact: IContactBase, bulkMode: Boolean) {
        if (bulkMode) {
            screenContext.contactListViewModel.toggleContactSelected(contact)
        } else {
            screenContext.navigateToContactDetailScreen(contact)
        }
    }

    private fun longClickContact(screenContext: IContactListScreenContext, contact: IContactBase) {
        screenContext.contactListViewModel.setBulkMode(enabled = true)
        selectContact(screenContext, contact, bulkMode = true)
    }

    private fun createContact(screenContext: IContactListScreenContext) {
        screenContext.navigateToContactCreateScreen()
    }
}
