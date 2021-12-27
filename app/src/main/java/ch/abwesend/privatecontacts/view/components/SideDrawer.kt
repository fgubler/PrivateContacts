package ch.abwesend.privatecontacts.view.components

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.view.routing.AppRouter
import ch.abwesend.privatecontacts.view.routing.Screen
import ch.abwesend.privatecontacts.view.routing.sideDrawerScreens

@Composable
fun SideDrawerContent(router: AppRouter, selectedScreen: Screen) {
    val scrollState = rememberScrollState()
    val clickListener = createElementClickListener(router, selectedScreen)

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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Rounded.AccountCircle,
            contentDescription = stringResource(id = R.string.app_name)
        )
        Text(
            text = stringResource(id = R.string.app_name),
            modifier = Modifier
                .padding(start = 20.dp)
                .weight(1.0f),
            color = MaterialTheme.colors.primary,
            style = MaterialTheme.typography.h6
        )
    }
}

@Composable
fun SideDrawerElements(selectedScreen: Screen, clickListener: (Screen) -> Unit) {
    Text(
        text = stringResource(id = R.string.navigation),
        style = MaterialTheme.typography.subtitle1,
        modifier = Modifier.padding(bottom = 5.dp)
    )
    sideDrawerScreens.forEach { screen ->
        SideDrawerElement(
            titleRes = screen.titleRes,
            icon = screen.icon,
            isSelected = screen == selectedScreen,
            onClick = { clickListener(screen) }
        )
    }
}


@Composable
fun SideDrawerElement(
    @StringRes titleRes: Int,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val modifier =
        if (isSelected) Modifier.background(color = MaterialTheme.colors.primary)
        else Modifier.background(color = MaterialTheme.colors.background)

    val fontColor =
        if (isSelected) MaterialTheme.colors.onPrimary
        else MaterialTheme.colors.onBackground

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(10.dp)
            .clickable { onClick() },
    ) {
        Icon(
            imageVector = icon,
            contentDescription = stringResource(id = titleRes),
            modifier = modifier,
            tint = fontColor,
        )
        Text(
            text = stringResource(id = titleRes),
            color = fontColor,
            modifier = modifier
                .padding(start = 20.dp)
                .weight(1.0f)
        )
    }
}

private fun createElementClickListener(router: AppRouter, selectedScreen: Screen): (Screen) -> Unit {
    val logger = selectedScreen.logger
    return { screen: Screen ->
        val isSelected = screen == selectedScreen

        if (isSelected) {
            logger.debug("Screen '${screen.key}' is already selected")
        } else {
            logger.debug("Navigating from screen '${selectedScreen.key}' to '$screen.key'")
            router.navigateToScreen(screen)
        }
    }
}
