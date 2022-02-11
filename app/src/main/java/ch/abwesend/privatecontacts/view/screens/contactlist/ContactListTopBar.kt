package ch.abwesend.privatecontacts.view.screens.contactlist

import androidx.compose.foundation.clickable
import androidx.compose.material.IconButton
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.view.components.CancelIcon
import ch.abwesend.privatecontacts.view.components.SearchIcon
import ch.abwesend.privatecontacts.view.components.buttons.BackIconButton
import ch.abwesend.privatecontacts.view.components.buttons.MenuButton
import ch.abwesend.privatecontacts.view.viewmodel.ContactListViewModel
import kotlinx.coroutines.CoroutineScope

@Composable
fun ContactListScreen.ContactListTopBar(
    viewModel: ContactListViewModel,
    scaffoldState: ScaffoldState,
    coroutineScope: CoroutineScope,
) {
    var showSearch by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }

    val backgroundColor = if (showSearch) Color.White else MaterialTheme.colors.primary

    TopAppBar(
        backgroundColor = backgroundColor,
        title = {
            if (showSearch) {
                SearchField(searchText) {
                    searchText = it
                    viewModel.changeSearchQuery(it)
                }
            } else {
                Text(text = stringResource(id = R.string.screen_contact_list))
            }
        },
        navigationIcon = {
            if (showSearch) {
                BackIconButton {
                    showSearch = false
                    searchText = ""
                    viewModel.reloadContacts()
                }
            } else {
                MenuButton(scaffoldState = scaffoldState, coroutineScope = coroutineScope)
            }
        },
        actions = {
            if (!showSearch) {
                IconButton(onClick = { showSearch = true }) { SearchIcon() }
            }
        }
    )
}

@Composable
private fun SearchField(query: String, onQueryChanged: (String) -> Unit) {
    val focusRequester = remember { FocusRequester() }

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
        }
    )

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}
