/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.screens

import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.stringResource
import ch.abwesend.privatecontacts.view.components.SideDrawerContent
import ch.abwesend.privatecontacts.view.components.buttons.MenuButton
import ch.abwesend.privatecontacts.view.model.ScreenContext
import ch.abwesend.privatecontacts.view.routing.Screen
import kotlinx.coroutines.CoroutineScope

@Composable
fun BaseScreen(
    screenContext: ScreenContext,
    selectedScreen: Screen,
    scaffoldState: ScaffoldState = rememberScaffoldState(),
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    topBar: @Composable () -> Unit = {
        BaseTopBar(
            selectedScreen = selectedScreen,
            scaffoldState = scaffoldState,
            coroutineScope = coroutineScope,
        )
    },
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable () -> Unit,
) {
    Scaffold(
        scaffoldState = scaffoldState,
        topBar = topBar,
        drawerContent = { SideDrawerContent(screenContext.router, selectedScreen) },
        floatingActionButton = floatingActionButton,
        content = { content() },
    )
}

@Composable
fun BaseTopBar(
    selectedScreen: Screen,
    scaffoldState: ScaffoldState,
    coroutineScope: CoroutineScope,
) {
    TopAppBar(
        title = { Text(text = stringResource(id = selectedScreen.titleRes)) },
        navigationIcon = {
            MenuButton(scaffoldState = scaffoldState, coroutineScope = coroutineScope)
        },
    )
}
