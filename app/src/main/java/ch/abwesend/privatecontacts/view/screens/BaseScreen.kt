/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.DrawerState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ch.abwesend.privatecontacts.view.components.SideDrawerContent
import ch.abwesend.privatecontacts.view.components.buttons.MenuBackButton
import ch.abwesend.privatecontacts.view.theme.appTopAppBarColors
import ch.abwesend.privatecontacts.view.components.buttons.MenuButton
import ch.abwesend.privatecontacts.view.model.screencontext.IScreenContextBase
import ch.abwesend.privatecontacts.view.model.screencontext.isGenericNavigationAllowed
import ch.abwesend.privatecontacts.view.routing.Screen
import ch.abwesend.privatecontacts.view.util.setMainContentSafeAreaPadding
import kotlinx.coroutines.CoroutineScope
import kotlin.contracts.ExperimentalContracts

private val hidden: @Composable () -> Unit = {}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalContracts::class)
@Composable
fun BaseScreen(
    screenContext: IScreenContextBase,
    selectedScreen: Screen,
    /** if false, only back-navigation is allowed */
    allowFullNavigation: Boolean = false,
    invertTopAndBottomBars: Boolean = screenContext.settings.invertTopAndBottomBars,
    drawerState: DrawerState = rememberDrawerState(DrawerValue.Closed),
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    topBarActions: @Composable RowScope.() -> Unit = {},
    topBar: @Composable () -> Unit = {
        BaseTopBar(
            screenContext = screenContext,
            selectedScreen = selectedScreen,
            drawerState = drawerState,
            coroutineScope = coroutineScope,
            allowFullNavigation = allowFullNavigation && isGenericNavigationAllowed(screenContext),
            actions = topBarActions,
        )
    },
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    val enableSideDrawer = allowFullNavigation && isGenericNavigationAllowed(screenContext)

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = enableSideDrawer,
        drawerContent = {
            if (enableSideDrawer) {
                ModalDrawerSheet {
                    SideDrawerContent(
                        selectedScreen = selectedScreen,
                        drawerState = drawerState,
                        navigate = screenContext::navigateToSelfInitializingScreen,
                    )
                }
            }
        },
    ) {
        Scaffold(
            topBar = if (invertTopAndBottomBars) hidden else topBar,
            bottomBar = if (invertTopAndBottomBars) topBar else hidden,
            modifier = Modifier
                .setMainContentSafeAreaPadding(invertTopAndBottomBars, addHorizontalPadding = false),
            floatingActionButton = floatingActionButton,
            content = { padding -> content(padding) },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BaseTopBar(
    screenContext: IScreenContextBase,
    selectedScreen: Screen,
    drawerState: DrawerState,
    coroutineScope: CoroutineScope,
    allowFullNavigation: Boolean,
    actions: @Composable RowScope.() -> Unit = {},
) {
    TopAppBar(
        title = { Text(text = stringResource(id = selectedScreen.titleRes)) },
        navigationIcon = {
            if (allowFullNavigation) {
                MenuButton(drawerState = drawerState, coroutineScope = coroutineScope)
            } else {
                MenuBackButton(onBackButtonClicked = screenContext::navigateUp)
            }
        },
        actions = actions,
        colors = appTopAppBarColors(),
    )
}
