/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.stringResource
import ch.abwesend.privatecontacts.view.components.SideDrawerContent
import ch.abwesend.privatecontacts.view.components.buttons.MenuBackButton
import ch.abwesend.privatecontacts.view.components.buttons.MenuButton
import ch.abwesend.privatecontacts.view.model.ScreenContext
import ch.abwesend.privatecontacts.view.routing.Screen
import kotlinx.coroutines.CoroutineScope

private val hidden: @Composable () -> Unit = {}

@Composable
fun BaseScreen(
    screenContext: ScreenContext,
    selectedScreen: Screen,
    /** if false, only back-navigation is allowed */
    allowFullNavigation: Boolean = false,
    invertTopAndBottomBars: Boolean = screenContext.settings.invertTopAndBottomBars,
    scaffoldState: ScaffoldState = rememberScaffoldState(),
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    topBarActions: @Composable RowScope.() -> Unit = {},
    topBar: @Composable () -> Unit = {
        BaseTopBar(
            screenContext = screenContext,
            selectedScreen = selectedScreen,
            allowFullNavigation = allowFullNavigation,
            actions = topBarActions,
            scaffoldState = scaffoldState,
            coroutineScope = coroutineScope,
        )
    },
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        scaffoldState = scaffoldState,
        topBar = if (invertTopAndBottomBars) hidden else topBar,
        bottomBar = if (invertTopAndBottomBars) topBar else hidden,
        drawerContent = { SideDrawerContent(selectedScreen, scaffoldState, screenContext.router::navigateToScreen) },
        floatingActionButton = floatingActionButton,
        content = { padding -> content(padding) },
    )
}

@Composable
private fun BaseTopBar(
    screenContext: ScreenContext,
    selectedScreen: Screen,
    allowFullNavigation: Boolean,
    actions: @Composable RowScope.() -> Unit = {},
    scaffoldState: ScaffoldState,
    coroutineScope: CoroutineScope,
) {
    TopAppBar(
        title = { Text(text = stringResource(id = selectedScreen.titleRes)) },
        navigationIcon = {
            if (allowFullNavigation) {
                MenuButton(scaffoldState = scaffoldState, coroutineScope = coroutineScope)
            } else {
                MenuBackButton(router = screenContext.router)
            }
        },
        actions = actions,
    )
}
