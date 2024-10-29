/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import ch.abwesend.privatecontacts.view.components.buttons.MenuBackButton
import ch.abwesend.privatecontacts.view.components.buttons.MenuButton
import ch.abwesend.privatecontacts.view.model.screencontext.IScreenContextBase
import ch.abwesend.privatecontacts.view.model.screencontext.isGenericNavigationAllowed
import ch.abwesend.privatecontacts.view.routing.Screen
import ch.abwesend.privatecontacts.view.util.setMainContentSafeAreaPadding
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
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    topBarActions: @Composable RowScope.() -> Unit = {},
    topBar: @Composable () -> Unit = {
        BaseTopBar(
            screenContext = screenContext,
            selectedScreen = selectedScreen,
            coroutineScope = coroutineScope,
            allowFullNavigation = allowFullNavigation && isGenericNavigationAllowed(screenContext),
            invertTopAndBottomBars = invertTopAndBottomBars,
            actions = topBarActions,
        )
    },
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        topBar = if (invertTopAndBottomBars) hidden else topBar,
        bottomBar = if (invertTopAndBottomBars) topBar else hidden,
        modifier = Modifier
            .background(Color.White)
            .setMainContentSafeAreaPadding(invertTopAndBottomBars),
        floatingActionButton = floatingActionButton,
        content = { padding -> content(padding) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BaseTopBar(
    screenContext: IScreenContextBase,
    selectedScreen: Screen,
    coroutineScope: CoroutineScope,
    allowFullNavigation: Boolean,
    invertTopAndBottomBars: Boolean,
    actions: @Composable RowScope.() -> Unit = {},
) {
    TopAppBar(
        title = { Text(text = stringResource(id = selectedScreen.titleRes)) },
        navigationIcon = {
            if (allowFullNavigation) {
                MenuButton(coroutineScope = coroutineScope)
            } else {
                MenuBackButton(onBackButtonClicked = screenContext::navigateUp)
            }
        },
        modifier = Modifier.setTopBarSafeAreaPadding(invertTopAndBottomBars),
        actions = actions,
    )
}
