/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.routing

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContactPage
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import ch.abwesend.privatecontacts.R

enum class Screen(
    val titleRes: Int,
    val icon: ImageVector,
    val key: String,
    val showInSideDrawer: Boolean,
) {
    ContactList(
        titleRes = R.string.screen_contact_list,
        icon = Icons.Default.Contacts,
        key = "ContactListScreen",
        showInSideDrawer = true,
    ),

    Settings(
        titleRes = R.string.screen_settings,
        icon = Icons.Default.Settings,
        key = "SettingsScreen",
        showInSideDrawer = true,
    ),

    AboutTheApp(
        titleRes = R.string.screen_about_the_app,
        icon = Icons.Default.Info,
        key = "AboutTheAppScreen",
        showInSideDrawer = true,
    ),

    ContactDetail(
        titleRes = R.string.screen_contact_details,
        icon = Icons.Default.ContactPage,
        key = "ContactDetailScreen",
        showInSideDrawer = false,
    ),

    ContactEdit(
        titleRes = R.string.screen_contact_edit,
        icon = Icons.Default.ContactPage,
        key = "ContactEditScreen",
        showInSideDrawer = false,
    );

    companion object {
        val sideDrawerScreens: List<Screen> =
            values().filter { it.showInSideDrawer }
    }
}
