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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DrawerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import ch.abwesend.privatecontacts.BuildConfig
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.view.routing.Screen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun SideDrawerContent(selectedScreen: Screen, drawerState: DrawerState, navigate: (Screen) -> Unit) {
    val scrollState = rememberScrollState()
    val clickListener = createElementClickListener(
        selectedScreen = selectedScreen,
        coroutineScope = rememberCoroutineScope(),
        drawerState = drawerState,
        navigate = navigate,
    )

    Column(
        modifier = Modifier
            .verticalScroll(scrollState)
            .fillMaxSize()
    ) {
        SideDrawerHeader()

        HorizontalDivider(modifier = Modifier.padding(bottom = 20.dp))

        SideDrawerElements(selectedScreen, clickListener)

        SideDrawerFooter()
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
            modifier = Modifier
                .widthIn(max = 500.dp)
                .clip(RoundedCornerShape(20.dp))
        )
        Text(
            text = stringResource(id = R.string.app_name),
            modifier = Modifier.padding(top = 10.dp),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleLarge
        )
    }
}

@Composable
fun SideDrawerFooter() {
    val version = remember { BuildConfig.VERSION_NAME }
    Column(
        verticalArrangement = Arrangement.Bottom,
        modifier = Modifier.fillMaxHeight()
    ) {
        Row(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            val versionText = "${stringResource(id = R.string.version)} $version"
            Text(text = versionText)
        }
    }
}

@Composable
fun SideDrawerElements(selectedScreen: Screen, clickListener: (Screen) -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Screen.sideDrawerScreens.forEach { screen ->
            SideDrawerElement(
                titleRes = screen.titleRes,
                icon = screen.icon,
                isSelected = screen == selectedScreen,
                onClick = { clickListener(screen) }
            )
        }
    }
}

@Composable
private fun SideDrawerElement(
    @StringRes titleRes: Int,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val color =
        if (isSelected) MaterialTheme.colorScheme.primary
        else Color.Transparent

    val fontColor =
        if (isSelected) MaterialTheme.colorScheme.onPrimary
        else MaterialTheme.colorScheme.onBackground

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
    selectedScreen: Screen,
    coroutineScope: CoroutineScope,
    drawerState: DrawerState,
    navigate: (Screen) -> Unit,
): (Screen) -> Unit {
    val logger = selectedScreen.logger
    return { screen: Screen ->
        if (screen == selectedScreen) {
            logger.info("Screen '${screen.key}' is already selected")
        } else {
            logger.info("Navigating from screen '${selectedScreen.key}' to '$screen.key'")
            navigate(screen)
            coroutineScope.launch { drawerState.close() }
        }
    }
}
