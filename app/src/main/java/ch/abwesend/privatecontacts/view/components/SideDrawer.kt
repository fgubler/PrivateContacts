/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.components

import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.view.routing.AppRouter
import ch.abwesend.privatecontacts.view.routing.Screen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun SideDrawerContent(router: AppRouter, selectedScreen: Screen, scaffoldState: ScaffoldState) {
    val scrollState = rememberScrollState()
    val clickListener = createElementClickListener(
        router = router,
        selectedScreen = selectedScreen,
        coroutineScope = rememberCoroutineScope(),
        scaffoldState = scaffoldState,
    )

    Column(
        modifier = Modifier.verticalScroll(scrollState)
    ) {
        SideDrawerHeader()

        Divider(modifier = Modifier.padding(bottom = 20.dp))

        SideDrawerElements(selectedScreen, clickListener)
    }
}

@Composable
fun SideDrawerHeader() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
    ) {
        Image(
            bitmap = ImageBitmap.imageResource(id = R.drawable.app_logo_large),
            contentDescription = stringResource(id = R.string.app_name),
            modifier = Modifier.widthIn(max = 500.dp).clip(RoundedCornerShape(20.dp))
        )
        Text(
            text = stringResource(id = R.string.app_name),
            modifier = Modifier.padding(top = 10.dp),
            color = MaterialTheme.colors.primary,
            style = MaterialTheme.typography.h6
        )
    }
}

@Composable
fun SideDrawerElements(selectedScreen: Screen, clickListener: (Screen) -> Unit) {
    Screen.sideDrawerScreens.forEach { screen ->
        SideDrawerElement(
            titleRes = screen.titleRes,
            icon = screen.icon,
            isSelected = screen == selectedScreen,
            onClick = { clickListener(screen) }
        )
    }
}

@Composable
private fun SideDrawerListHeader(@StringRes titleRes: Int) {
    Text(
        text = stringResource(id = titleRes),
        style = MaterialTheme.typography.subtitle1,
        modifier = Modifier.padding(start = 5.dp, bottom = 5.dp)
    )
}

@Composable
private fun SideDrawerElement(
    @StringRes titleRes: Int,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val color =
        if (isSelected) MaterialTheme.colors.primary
        else Color.Transparent // just show the background of the parent

    val fontColor =
        if (isSelected) MaterialTheme.colors.onPrimary
        else MaterialTheme.colors.onBackground

    val fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal

    Surface(
        modifier = Modifier
            .padding(end = 10.dp)
            .clickable { onClick() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(topEnd = 5.dp, bottomEnd = 5.dp))
                .background(color = color)
                .fillMaxWidth()
                .height(50.dp)
                .padding(horizontal = 10.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = stringResource(id = titleRes),
                tint = fontColor,
            )
            Text(
                text = stringResource(id = titleRes),
                color = fontColor,
                fontWeight = fontWeight,
                modifier = Modifier
                    .padding(start = 20.dp)
                    .weight(1.0f)
            )
        }
    }
}

private fun createElementClickListener(
    router: AppRouter,
    selectedScreen: Screen,
    coroutineScope: CoroutineScope,
    scaffoldState: ScaffoldState,
): (Screen) -> Unit {
    val logger = selectedScreen.logger
    return { screen: Screen ->
        if (screen == selectedScreen) {
            logger.info("Screen '${screen.key}' is already selected")
        } else {
            logger.info("Navigating from screen '${selectedScreen.key}' to '$screen.key'")
            router.navigateToScreen(screen)
            coroutineScope.launch { scaffoldState.drawerState.close() }
        }
    }
}
