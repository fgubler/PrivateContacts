package ch.abwesend.privatecontacts.view.screens.contactlist

import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.stringResource
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.lib.coroutine.IDispatchers
import ch.abwesend.privatecontacts.domain.lib.flow.AsyncResource
import ch.abwesend.privatecontacts.domain.model.contact.ContactBase
import ch.abwesend.privatecontacts.domain.util.applicationScope
import ch.abwesend.privatecontacts.domain.util.getAnywhere
import ch.abwesend.privatecontacts.view.components.FullScreenError
import ch.abwesend.privatecontacts.view.components.LoadingIndicatorFullScreen
import ch.abwesend.privatecontacts.view.components.SideDrawerContent
import ch.abwesend.privatecontacts.view.components.buttons.MenuButton
import ch.abwesend.privatecontacts.view.components.config.ButtonConfig
import ch.abwesend.privatecontacts.view.model.ScreenContext
import ch.abwesend.privatecontacts.view.routing.Screen
import ch.abwesend.privatecontacts.view.util.composeIfError
import ch.abwesend.privatecontacts.view.util.composeIfLoading
import ch.abwesend.privatecontacts.view.util.composeIfReady
import ch.abwesend.privatecontacts.view.util.getLogger
import ch.abwesend.privatecontacts.view.viewmodel.ContactListViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ContactListScreen(screenContext: ScreenContext) {
    val scaffoldState = rememberScaffoldState()
    val coroutineScope = rememberCoroutineScope()

    val router = screenContext.router

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = { ContactListTopBar(scaffoldState, coroutineScope) },
        drawerContent = { SideDrawerContent(router, Screen.ContactList) },
        floatingActionButton = { AddContactButton(screenContext) }
    ) {
        ContactListContent(screenContext)
    }
}

@Composable
private fun ContactListTopBar(scaffoldState: ScaffoldState, coroutineScope: CoroutineScope) {
    TopAppBar(
        title = { Text(text = stringResource(id = R.string.screen_contact_list)) },
        navigationIcon = { MenuButton(scaffoldState = scaffoldState, coroutineScope = coroutineScope) }
    )
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
    LaunchedEffect(Unit) {
        viewModel.loadContacts()
    }

    val contactsResource: AsyncResource<List<ContactBase>> by viewModel.contacts.collectAsState()

    contactsResource
        .composeIfLoading {
            getLogger().debug("Loading contacts")
            LoadingIndicatorFullScreen(
                textAfterIndicator = { stringResource(id = R.string.loading_contacts) }
            )
        }
        .composeIfError {
            LoadingError(viewModel)
        }
        .composeIfReady { contacts ->
            ContactList(contacts = contacts) { contact ->
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
        ) { viewModel.loadContacts() },
    )
}

private fun selectContact(screenContext: ScreenContext, contact: ContactBase) {
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
    screenContext.contactEditViewModel.createNewContact()
    screenContext.router.navigateToScreen(Screen.ContactEdit)
}
