package ch.abwesend.privatecontacts.view.screens.contactlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.lib.flow.AsyncResource
import ch.abwesend.privatecontacts.domain.model.ContactBase
import ch.abwesend.privatecontacts.view.components.LoadingIndicatorFullScreen
import ch.abwesend.privatecontacts.view.components.MenuButton
import ch.abwesend.privatecontacts.view.components.SideDrawerContent
import ch.abwesend.privatecontacts.view.routing.AppRouter
import ch.abwesend.privatecontacts.view.routing.Screen
import ch.abwesend.privatecontacts.view.util.composeIfError
import ch.abwesend.privatecontacts.view.util.composeIfLoading
import ch.abwesend.privatecontacts.view.util.composeIfReady
import ch.abwesend.privatecontacts.view.util.getLogger
import ch.abwesend.privatecontacts.view.viewmodel.ContactListViewModel
import kotlinx.coroutines.CoroutineScope

@Composable
fun ContactListScreen(router: AppRouter, viewModel: ContactListViewModel) {
    val scaffoldState = rememberScaffoldState()
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = { ContactListTopBar(scaffoldState, coroutineScope) },
        drawerContent = { SideDrawerContent(router, Screen.ContactList) },
        floatingActionButton = { AddContactButton(router = router, viewModel = viewModel) }
    ) {
        ContactListContent(router = router, viewModel = viewModel)
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
private fun AddContactButton(router: AppRouter, viewModel: ContactListViewModel) {
    FloatingActionButton(onClick = { createContact(router, viewModel) }) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = stringResource(id = R.string.new_contact),
            tint = MaterialTheme.colors.onSecondary,
        )
    }
}

@Composable
private fun ContactListContent(router: AppRouter, viewModel: ContactListViewModel) {
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
            LoadingError()
        }
        .composeIfReady { contacts ->
            ContactList(contacts = contacts) { contact ->
                selectContact(router, viewModel, contact)
            }
        }
}

@Composable
private fun LoadingError() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text(text = stringResource(id = R.string.data_loading_error))
    }
}

private fun selectContact(router: AppRouter, viewModel: ContactListViewModel, contact: ContactBase) {
    // TODO implement
}

private fun createContact(router: AppRouter, viewModel: ContactListViewModel) {
    viewModel.createContact()
    router.navigateToScreen(Screen.ContactDetail)
}
