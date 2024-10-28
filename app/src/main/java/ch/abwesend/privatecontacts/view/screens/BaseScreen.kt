/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.abwesend.privatecontacts.view.components.SideDrawerContent
import ch.abwesend.privatecontacts.view.components.buttons.MenuBackButton
import ch.abwesend.privatecontacts.view.components.buttons.MenuButton
import ch.abwesend.privatecontacts.view.model.screencontext.IScreenContextBase
import ch.abwesend.privatecontacts.view.model.screencontext.isGenericNavigationAllowed
import ch.abwesend.privatecontacts.view.routing.Screen
import ch.abwesend.privatecontacts.view.util.getSafeAreaPadding
import ch.abwesend.privatecontacts.view.util.setTopBarSafeAreaPadding
import kotlin.contracts.ExperimentalContracts
import kotlinx.coroutines.CoroutineScope

private val hidden: @Composable () -> Unit = {}

@ExperimentalContracts
@Composable
fun BaseScreen(
    screenContext: IScreenContextBase,
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
            scaffoldState = scaffoldState,
            coroutineScope = coroutineScope,
            allowFullNavigation = allowFullNavigation && isGenericNavigationAllowed(screenContext),
            invertTopAndBottomBars = invertTopAndBottomBars,
            actions = topBarActions,
        )
    },
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    val safeArea = getSafeAreaPadding()
    val layout = LocalLayoutDirection.current
    val modifier = Modifier.padding(
        top = if (invertTopAndBottomBars) safeArea.calculateTopPadding() else 0.dp,
        start = safeArea.calculateStartPadding(layout),
        end = safeArea.calculateEndPadding(layout),
        bottom = if (invertTopAndBottomBars) 0.dp else safeArea.calculateBottomPadding(),
    )

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = if (invertTopAndBottomBars) hidden else topBar,
        bottomBar = if (invertTopAndBottomBars) topBar else hidden,
        modifier = modifier,
        floatingActionButton = floatingActionButton,
        drawerContent = {
            if (allowFullNavigation && isGenericNavigationAllowed(screenContext)) {
                SideDrawerContent(
                    selectedScreen = selectedScreen,
                    scaffoldState = scaffoldState,
                    navigate = screenContext::navigateToSelfInitializingScreen,
                )
            }
        },
        content = { padding -> content(padding) },
    )
}

@Composable
private fun BaseTopBar(
    screenContext: IScreenContextBase,
    selectedScreen: Screen,
    scaffoldState: ScaffoldState,
    coroutineScope: CoroutineScope,
    allowFullNavigation: Boolean,
    invertTopAndBottomBars: Boolean,
    actions: @Composable RowScope.() -> Unit = {},
) {
    TopAppBar(
        title = { Text(text = stringResource(id = selectedScreen.titleRes)) },
        navigationIcon = {
            if (allowFullNavigation) {
                MenuButton(scaffoldState = scaffoldState, coroutineScope = coroutineScope)
            } else {
                MenuBackButton(onBackButtonClicked = screenContext::navigateUp)
            }
        },
        modifier = Modifier.setTopBarSafeAreaPadding(invertTopAndBottomBars),
        actions = actions,
    )
}
