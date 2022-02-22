/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.components

import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Expand
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sync
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ch.abwesend.privatecontacts.R

@Composable
fun CancelIcon(modifier: Modifier = Modifier) {
    Icon(
        imageVector = Icons.Default.Close,
        contentDescription = stringResource(id = R.string.cancel),
        modifier = modifier,
    )
}

@Composable
fun SaveIcon() {
    Icon(
        imageVector = Icons.Default.Save,
        contentDescription = stringResource(id = R.string.save),
    )
}

@Composable
fun SearchIcon() {
    Icon(
        imageVector = Icons.Default.Search,
        contentDescription = stringResource(id = R.string.search)
    )
}

@Composable
fun RefreshIcon() {
    Icon(
        imageVector = Icons.Default.Sync,
        contentDescription = stringResource(id = R.string.search)
    )
}

@Composable
fun BackIcon() {
    Icon(
        imageVector = Icons.Default.ArrowBack,
        contentDescription = stringResource(id = R.string.back)
    )
}

@Composable
fun EditIcon() {
    Icon(
        imageVector = Icons.Default.Edit,
        contentDescription = stringResource(id = R.string.edit)
    )
}

/** The 3-dot icon for settings / more actions */
@Composable
fun MoreActionsIcon() {
    Icon(
        imageVector = Icons.Default.MoreVert,
        contentDescription = stringResource(id = R.string.edit)
    )
}

@Composable
fun ExpandIcon() {
    Icon(
        imageVector = Icons.Default.Expand,
        contentDescription = stringResource(id = R.string.expand),
    )
}

@Composable
fun CompressIcon() {
    Icon(
        imageVector = Icons.Default.Compress,
        contentDescription = stringResource(id = R.string.expand),
    )
}
