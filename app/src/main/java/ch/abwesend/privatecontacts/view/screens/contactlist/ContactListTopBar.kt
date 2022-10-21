/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.screens.contactlist

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
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
import ch.abwesend.privatecontacts.domain.model.contact.ContactType.SECRET
import ch.abwesend.privatecontacts.domain.model.contact.IContactBase
import ch.abwesend.privatecontacts.view.components.CancelIcon
import ch.abwesend.privatecontacts.view.components.SearchIcon
import ch.abwesend.privatecontacts.view.components.buttons.BackIconButton
import ch.abwesend.privatecontacts.view.components.buttons.CancelIconButton
import ch.abwesend.privatecontacts.view.components.buttons.MenuButton
import ch.abwesend.privatecontacts.view.components.buttons.MoreActionsIconButton
import ch.abwesend.privatecontacts.view.components.buttons.RefreshIconButton
import ch.abwesend.privatecontacts.view.components.buttons.SearchIconButton
import ch.abwesend.privatecontacts.view.components.dialogs.YesNoDialog
import ch.abwesend.privatecontacts.view.model.ContactListScreenState.BulkMode
import ch.abwesend.privatecontacts.view.model.ContactListScreenState.Normal
import ch.abwesend.privatecontacts.view.model.ContactListScreenState.Search
import ch.abwesend.privatecontacts.view.util.createKeyboardAndFocusManager
import ch.abwesend.privatecontacts.view.viewmodel.ContactListViewModel
import kotlinx.coroutines.FlowPreview

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
    TopAppBar(
        backgroundColor = Color.White,
        title = { SearchField(searchText) { changeSearchText(it) } },
        navigationIcon = { BackIconButton { resetSearch() } },
    )
    BackHandler { resetSearch() }
}

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
            ActionsMenu(
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
fun ActionsMenu(
    viewModel: ContactListViewModel,
    selectedContacts: Set<IContactBase>,
    expanded: Boolean,
    onCloseMenu: () -> Unit,
) {
    if (selectedContacts.isNotEmpty()) {
        DropdownMenu(expanded = expanded, onDismissRequest = onCloseMenu) {
            TypeChangeMenuItem(viewModel, selectedContacts, onCloseMenu)
            DeleteMenuItem(viewModel, selectedContacts, onCloseMenu)
        }
    }
}

@Composable
private fun TypeChangeMenuItem(
    viewModel: ContactListViewModel,
    contacts: Set<IContactBase>,
    onCloseMenu: () -> Unit,
) {
    if (contacts.any { it.type != SECRET }) {
        var confirmationDialogVisible: Boolean by remember { mutableStateOf(false) }

        DropdownMenuItem(onClick = { confirmationDialogVisible = true }) {
            Text(stringResource(id = R.string.make_contacts_secret))
        }

        TypeChangeConfirmationDialog(
            viewModel = viewModel,
            contacts = contacts,
            visible = confirmationDialogVisible,
            hideDialog = {
                confirmationDialogVisible = false
                onCloseMenu()
            },
        )
    }
}

@Composable
private fun TypeChangeConfirmationDialog(
    viewModel: ContactListViewModel,
    contacts: Set<IContactBase>,
    visible: Boolean,
    hideDialog: () -> Unit,
) {
    if (visible) {
        YesNoDialog(
            title = R.string.make_contacts_secret_title,
            text = R.string.make_contact_secret_text,
            onYes = {
                hideDialog()
                viewModel.changeContactType(contacts, SECRET)
            },
            onNo = hideDialog
        )
    }
}

@Composable
private fun DeleteMenuItem(
    viewModel: ContactListViewModel,
    selectedContacts: Set<IContactBase>,
    onCloseMenu: () -> Unit,
) {
    var deleteConfirmationDialogVisible: Boolean by remember { mutableStateOf(false) }
    val multipleContacts = selectedContacts.size > 1

    DropdownMenuItem(onClick = { deleteConfirmationDialogVisible = true }) {
        @StringRes val text = if (multipleContacts) R.string.delete_contacts else R.string.delete_contact
        Text(stringResource(id = text))
    }

    DeleteConfirmationDialog(
        viewModel = viewModel,
        selectedContacts = selectedContacts,
        visible = deleteConfirmationDialogVisible,
        hideDialog = {
            deleteConfirmationDialogVisible = false
            onCloseMenu()
        },
    )
}

@Composable
private fun DeleteConfirmationDialog(
    viewModel: ContactListViewModel,
    selectedContacts: Set<IContactBase>,
    visible: Boolean,
    hideDialog: () -> Unit,
) {
    if (visible) {
        YesNoDialog(
            title = R.string.delete_contacts_title,
            text = { Text(text = stringResource(id = R.string.delete_contacts_text, selectedContacts.size)) },
            onYes = {
                hideDialog()
                val contactIds = selectedContacts.map { it.id }.toSet()
                viewModel.deleteContacts(contactIds)
            },
            onNo = hideDialog
        )
    }
}

@ExperimentalComposeUiApi
@Composable
private fun SearchField(query: String, onQueryChanged: (String) -> Unit) {
    val focusRequester = remember { FocusRequester() }
    val manager = createKeyboardAndFocusManager()

    TextField(
        value = query,
        onValueChange = onQueryChanged,
        maxLines = 1,
        colors = TextFieldDefaults.textFieldColors(backgroundColor = Color.White),
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
