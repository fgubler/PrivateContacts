/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.screens.contactlist

import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.res.stringResource
import androidx.paging.compose.collectAsLazyPagingItems
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.lib.coroutine.IDispatchers
import ch.abwesend.privatecontacts.domain.model.contact.IContactBase
import ch.abwesend.privatecontacts.domain.util.applicationScope
import ch.abwesend.privatecontacts.domain.util.getAnywhere
import ch.abwesend.privatecontacts.view.components.FullScreenError
import ch.abwesend.privatecontacts.view.components.LoadingIndicatorFullScreen
import ch.abwesend.privatecontacts.view.components.SideDrawerContent
import ch.abwesend.privatecontacts.view.model.ScreenContext
import ch.abwesend.privatecontacts.view.model.config.ButtonConfig
import ch.abwesend.privatecontacts.view.routing.Screen
import ch.abwesend.privatecontacts.view.util.isError
import ch.abwesend.privatecontacts.view.util.isLoading
import ch.abwesend.privatecontacts.view.viewmodel.ContactListViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@ExperimentalComposeUiApi
@FlowPreview
object ContactListScreen {

    @Composable
    fun Screen(screenContext: ScreenContext) {
        val scaffoldState = rememberScaffoldState()
        val coroutineScope = rememberCoroutineScope()

        val router = screenContext.router

        Scaffold(
            scaffoldState = scaffoldState,
            topBar = {
                ContactListTopBar(
                    viewModel = screenContext.contactListViewModel,
                    scaffoldState = scaffoldState,
                    coroutineScope = coroutineScope,
                )
            },
            drawerContent = { SideDrawerContent(router, Screen.ContactList) },
            floatingActionButton = { AddContactButton(screenContext) }
        ) {
            ContactListContent(screenContext)
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
        val pagedContacts = viewModel.contacts.value.collectAsLazyPagingItems()

        when {
            pagedContacts.isError -> LoadingError(viewModel)
            pagedContacts.isLoading -> LoadingIndicatorFullScreen(R.string.loading_contacts)
            else -> ContactList(pagedContacts = pagedContacts) { contact ->
                selectContact(screenContext, contact)
            }
        }
    }

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

    private fun selectContact(screenContext: ScreenContext, contact: IContactBase) {
        // TODO this should not be done in the view
        applicationScope.launch {
            val resolved = screenContext.contactListViewModel.resolveContact(contact)
            screenContext.contactEditViewModel.selectContact(resolved)
            val dispatchers: IDispatchers = getAnywhere()
            withContext(dispatchers.mainImmediate) {
                screenContext.router.navigateToScreen(Screen.ContactEdit)
            }
        }

        // TODO implement with read-only screen
    }

    private fun createContact(screenContext: ScreenContext) {
        screenContext.contactEditViewModel.createContact()
        screenContext.router.navigateToScreen(Screen.ContactEdit)
    }
}
