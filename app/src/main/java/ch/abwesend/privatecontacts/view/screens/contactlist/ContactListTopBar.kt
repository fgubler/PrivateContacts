/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.screens.contactlist

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.model.contact.IContactBase
import ch.abwesend.privatecontacts.domain.model.contact.withAccountInformation
import ch.abwesend.privatecontacts.view.components.CancelIcon
import ch.abwesend.privatecontacts.view.components.SearchIcon
import ch.abwesend.privatecontacts.view.components.buttons.BackIconButton
import ch.abwesend.privatecontacts.view.components.buttons.CancelIconButton
import ch.abwesend.privatecontacts.view.components.buttons.MenuButton
import ch.abwesend.privatecontacts.view.components.buttons.MoreActionsIconButton
import ch.abwesend.privatecontacts.view.components.buttons.RefreshIconButton
import ch.abwesend.privatecontacts.view.components.buttons.SearchIconButton
import ch.abwesend.privatecontacts.view.components.contactmenu.ChangeContactTypeMenuItem
import ch.abwesend.privatecontacts.view.components.contactmenu.DeleteContactMenuItem
import ch.abwesend.privatecontacts.view.components.contactmenu.ExportContactsMenuItem
import ch.abwesend.privatecontacts.view.model.ContactListScreenState.BulkMode
import ch.abwesend.privatecontacts.view.model.ContactListScreenState.Normal
import ch.abwesend.privatecontacts.view.model.ContactListScreenState.Search
import ch.abwesend.privatecontacts.view.model.ContactTypeChangeMenuConfig
import ch.abwesend.privatecontacts.view.util.createKeyboardAndFocusManager
import kotlinx.coroutines.FlowPreview

@OptIn(ExperimentalMaterial3Api::class)
@FlowPreview
@ExperimentalComposeUiApi
@Composable
fun ContactListTopBar(
    viewModel: ContactListViewModel,
    drawerState: DrawerState,
) {
    val screenStateState = viewModel.screenState

    when (val screenState = screenStateState.value) {
        is Normal -> NormalTopBar(
            drawerState = drawerState,
            reloadContacts = { viewModel.reloadContacts() },
            showSearch = { viewModel.showSearch() }
        )
        is Search -> SearchTopBar(
            searchText = screenState.searchText,
            changeSearchText = { viewModel.changeSearchQuery(it) },
            resetSearch = { viewModel.reloadContacts(resetSearch = true) }
        )
        is BulkMode -> BulkModeTopBar(
            viewModel = viewModel,
            selectedContacts = screenState.selectedContacts,
            disableBulkMode = { viewModel.setBulkMode(enabled = false) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NormalTopBar(
    drawerState: DrawerState,
    reloadContacts: () -> Unit,
    showSearch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    TopAppBar(
        title = { Text(text = stringResource(id = R.string.screen_contact_list)) },
        navigationIcon = { MenuButton(drawerState = drawerState, coroutineScope = coroutineScope) },
        modifier = modifier,
        actions = {
            RefreshIconButton { reloadContacts() }
            SearchIconButton { showSearch() }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@ExperimentalComposeUiApi
@Composable
private fun SearchTopBar(
    searchText: String,
    modifier: Modifier = Modifier,
    changeSearchText: (String) -> Unit,
    resetSearch: () -> Unit
) {
    val backgroundColor = MaterialTheme.colorScheme.background
    TopAppBar(
        title = { SearchField(searchText, backgroundColor) { changeSearchText(it) } },
        navigationIcon = { BackIconButton { resetSearch() } },
        modifier = modifier,
    )
    BackHandler { resetSearch() }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BulkModeTopBar(
    viewModel: ContactListViewModel,
    selectedContacts: Set<IContactBase>,
    modifier: Modifier = Modifier,
    disableBulkMode: () -> Unit
) {
    var dropDownMenuExpanded: Boolean by remember { mutableStateOf(false) }

    TopAppBar(
        title = { Text(text = stringResource(id = R.string.contact_list_bulk_mode_title, selectedContacts.size)) },
        navigationIcon = { CancelIconButton { disableBulkMode() } },
        modifier = modifier,
        actions = {
            MoreActionsIconButton { dropDownMenuExpanded = true }
            BulkModeActionsMenu(
                viewModel = viewModel,
                selectedContacts = selectedContacts,
                expanded = dropDownMenuExpanded
            ) {
                dropDownMenuExpanded = false
            }
        }
    )
    BackHandler { disableBulkMode() }
}

@Composable
fun BulkModeActionsMenu(
    viewModel: ContactListViewModel,
    selectedContacts: Set<IContactBase>,
    expanded: Boolean,
    onCloseMenu: () -> Unit,
) {
    val hasWritePermission = remember { viewModel.hasContactWritePermission }
    DropdownMenu(expanded = expanded, onDismissRequest = onCloseMenu) {
        DropdownMenuItem(
            text = { Text(stringResource(id = R.string.select_all)) },
            onClick = { viewModel.selectAllContacts() }
        )
        DropdownMenuItem(
            text = { Text(stringResource(id = R.string.deselect_all)) },
            onClick = { viewModel.deselectAllContacts() }
        )
        if (selectedContacts.isNotEmpty()) {
            HorizontalDivider()
            ContactType.entries.forEach { targetType ->
                ChangeContactTypeMenuItem(
                    viewModel = viewModel,
                    contacts = selectedContacts,
                    targetType = targetType,
                    enabled = hasWritePermission || !targetType.androidPermissionRequired,
                    onCloseMenu = onCloseMenu,
                )
            }
            DeleteMenuItem(viewModel, selectedContacts, onCloseMenu)
            HorizontalDivider()
            ExportMenuItem(viewModel, selectedContacts, onCloseMenu)
        }
    }
}

@Composable
private fun ChangeContactTypeMenuItem(
    viewModel: ContactListViewModel,
    contacts: Set<IContactBase>,
    targetType: ContactType,
    enabled: Boolean,
    onCloseMenu: () -> Unit,
) {
    if (contacts.any { it.type != targetType }) {
        val config = ContactTypeChangeMenuConfig.fromTargetType(targetType)
        val contactsWithAccountInformation = remember(contacts) {
            contacts.map { it.withAccountInformation() }
        }.toSet()

        ChangeContactTypeMenuItem(
            contacts = contactsWithAccountInformation,
            config = config,
            enabled = enabled,
        ) { changeContacts ->
            if (changeContacts) {
                viewModel.changeContactType(contactsWithAccountInformation, targetType)
            }
            onCloseMenu()
        }
    }
}

@Composable
private fun DeleteMenuItem(
    viewModel: ContactListViewModel,
    selectedContacts: Set<IContactBase>,
    onCloseMenu: () -> Unit,
) {
    DeleteContactMenuItem(numberOfContacts = selectedContacts.size) { delete ->
        if (delete) {
            val contactIds = selectedContacts.map { it.id }.toSet()
            viewModel.deleteContacts(contactIds)
        }
        onCloseMenu()
    }
}

@Composable
private fun ExportMenuItem(
    viewModel: ContactListViewModel,
    selectedContacts: Set<IContactBase>,
    onCloseMenu: () -> Unit,
) {
    ExportContactsMenuItem(
        contacts = selectedContacts,
        onCancel = onCloseMenu,
        onExportContact = { targetFile, vCardVersion ->
            viewModel.exportContacts(targetFile, vCardVersion, selectedContacts)
            onCloseMenu()
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@ExperimentalComposeUiApi
@Composable
private fun SearchField(query: String, backgroundColor: Color, onQueryChanged: (String) -> Unit) {
    val focusRequester = remember { FocusRequester() }
    val manager = createKeyboardAndFocusManager()

    TextField(
        value = query,
        onValueChange = onQueryChanged,
        maxLines = 1,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = backgroundColor,
            unfocusedContainerColor = backgroundColor,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
        ),
        modifier = Modifier.focusRequester(focusRequester),
        placeholder = { Text(text = stringResource(id = R.string.search)) },
        leadingIcon = { SearchIcon() },
        trailingIcon = {
            if (query.isNotEmpty()) {
                CancelIcon(Modifier.clickable { onQueryChanged("") })
            }
        },
        keyboardOptions = KeyboardOptions.Default.copy(
            imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions(onSearch = {
            manager.closeKeyboardAndClearFocus()
        })
    )

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}
