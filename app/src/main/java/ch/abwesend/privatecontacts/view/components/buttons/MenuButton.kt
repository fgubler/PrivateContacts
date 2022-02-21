/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.components.buttons

import androidx.compose.material.DrawerState
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.ScaffoldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ch.abwesend.privatecontacts.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun MenuButton(scaffoldState: ScaffoldState, coroutineScope: CoroutineScope) {
    IconButton(onClick = {
        coroutineScope.launch {
            scaffoldState.drawerState.toggle()
        }
    }) {
        Icon(
            imageVector = Icons.Default.Menu,
            contentDescription = stringResource(id = R.string.menu)
        )
    }
}

private suspend fun DrawerState.toggle() {
    if (isClosed) open()
    else close()
}
