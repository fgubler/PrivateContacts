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
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.TopAppBar
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
import ch.abwesend.privatecontacts.view.model.ContactListScreenState.BulkMode
import ch.abwesend.privatecontacts.view.model.ContactListScreenState.Normal
import ch.abwesend.privatecontacts.view.model.ContactListScreenState.Search
import ch.abwesend.privatecontacts.view.model.ContactTypeChangeMenuConfig
import ch.abwesend.privatecontacts.view.util.createKeyboardAndFocusManager
import ch.abwesend.privatecontacts.view.viewmodel.ContactListViewModel
import kotlinx.coroutines.FlowPreview

@ExperimentalMaterialApi
@FlowPreview
@ExperimentalComposeUiApi
@Composable
fun ContactListTopBar(
    viewModel: ContactListViewModel,
    scaffoldState: ScaffoldState,
) {
    val screenStateState = viewModel.screenState
    when (val screenState = screenStateState.value) {
        is Normal -> NormalTopBar(
            scaffoldState = scaffoldState,
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

@Composable
private fun NormalTopBar(
    scaffoldState: ScaffoldState,
    reloadContacts: () -> Unit,
    showSearch: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    TopAppBar(
        title = { Text(text = stringResource(id = R.string.screen_contact_list)) },
        navigationIcon = { MenuButton(scaffoldState = scaffoldState, coroutineScope = coroutineScope) },
        actions = {
            RefreshIconButton { reloadContacts() }
            SearchIconButton { showSearch() }
        }
    )
}

@ExperimentalComposeUiApi
@Composable
private fun SearchTopBar(
    searchText: String,
    changeSearchText: (String) -> Unit,
    resetSearch: () -> Unit
) {
    val backgroundColor = MaterialTheme.colors.background
    TopAppBar(
        backgroundColor = backgroundColor,
        title = { SearchField(searchText, backgroundColor) { changeSearchText(it) } },
        navigationIcon = { BackIconButton { resetSearch() } },
    )
    BackHandler { resetSearch() }
}

@ExperimentalMaterialApi
@Composable
private fun BulkModeTopBar(
    viewModel: ContactListViewModel,
    selectedContacts: Set<IContactBase>,
    disableBulkMode: () -> Unit
) {
    var dropDownMenuExpanded: Boolean by remember { mutableStateOf(false) }

    TopAppBar(
        title = { Text(text = stringResource(id = R.string.contact_list_bulk_mode_title, selectedContacts.size)) },
        navigationIcon = { CancelIconButton { disableBulkMode() } },
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

@ExperimentalMaterialApi
@Composable
fun BulkModeActionsMenu(
    viewModel: ContactListViewModel,
    selectedContacts: Set<IContactBase>,
    expanded: Boolean,
    onCloseMenu: () -> Unit,
) {
    val hasWritePermission = remember { viewModel.hasContactWritePermission }
    DropdownMenu(expanded = expanded, onDismissRequest = onCloseMenu) {
        DropdownMenuItem(onClick = { viewModel.selectAllContacts() }) {
            Text(stringResource(id = R.string.select_all))
        }
        DropdownMenuItem(onClick = { viewModel.deselectAllContacts() }) {
            Text(stringResource(id = R.string.deselect_all))
        }
        if (selectedContacts.isNotEmpty()) {
            Divider()
            ContactType.values().forEach { targetType ->
                ChangeContactTypeMenuItem(
                    viewModel = viewModel,
                    contacts = selectedContacts,
                    targetType = targetType,
                    enabled = hasWritePermission || !targetType.androidPermissionRequired,
                    onCloseMenu = onCloseMenu,
                )
            }
            DeleteMenuItem(viewModel, selectedContacts, onCloseMenu)
        }
    }
}

@ExperimentalMaterialApi
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
    DeleteContactMenuItem(contacts = selectedContacts) { delete ->
        if (delete) {
            val contactIds = selectedContacts.map { it.id }.toSet()
            viewModel.deleteContacts(contactIds)
        }
        onCloseMenu()
    }
}

@ExperimentalComposeUiApi
@Composable
private fun SearchField(query: String, backgroundColor: Color, onQueryChanged: (String) -> Unit) {
    val focusRequester = remember { FocusRequester() }
    val manager = createKeyboardAndFocusManager()

    TextField(
        value = query,
        onValueChange = onQueryChanged,
        maxLines = 1,
        colors = TextFieldDefaults.textFieldColors(backgroundColor = backgroundColor),
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
