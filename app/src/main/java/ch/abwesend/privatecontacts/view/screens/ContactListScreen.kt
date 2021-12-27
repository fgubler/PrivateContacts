package ch.abwesend.privatecontacts.view.screens

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.stringResource
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.view.components.SideDrawerContent
import ch.abwesend.privatecontacts.view.routing.AppRouter
import ch.abwesend.privatecontacts.view.routing.Screen
import ch.abwesend.privatecontacts.view.viewmodel.ContactListViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun ContactListScreen(router: AppRouter, viewModel: ContactListViewModel) {
    val scaffoldState = rememberScaffoldState()
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = { ContactListTopBar(scaffoldState, coroutineScope) },
        drawerContent = { SideDrawerContent(router, Screen.ContactList) },
        floatingActionButton = { AddContactButton(viewModel = viewModel) }
    ) {
    }
}

@Composable
fun ContactListTopBar(scaffoldState: ScaffoldState, coroutineScope: CoroutineScope) {
    TopAppBar(
        title = { Text(text = stringResource(id = R.string.screen_contact_list)) },
        navigationIcon = { MenuButton(scaffoldState = scaffoldState, coroutineScope = coroutineScope) }
    )
}

@Composable
fun MenuButton(scaffoldState: ScaffoldState, coroutineScope: CoroutineScope) {
    IconButton(onClick = {
        coroutineScope.launch { scaffoldState.drawerState.open() }
    }) {
        Icon(
            imageVector = Icons.Default.Menu,
            contentDescription = stringResource(id = R.string.menu)
        )
    }
}

@Composable
fun AddContactButton(viewModel: ContactListViewModel) {
    // TODO implement
}
