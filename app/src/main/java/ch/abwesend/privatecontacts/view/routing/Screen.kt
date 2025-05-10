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
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Output
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import ch.abwesend.privatecontacts.R

/**
 * [selfInitializing] if false, the screen needs data to be passed from the previous screen.
 * [showInSideDrawer] should only be true if [selfInitializing] is true.
 */
enum class Screen(
    val titleRes: Int,
    val icon: ImageVector,
    val key: String,
    val selfInitializing: Boolean,
    private val showInSideDrawer: Boolean,
) {
    ContactList(
        titleRes = R.string.screen_contact_list,
        icon = Icons.Default.Contacts,
        key = "ContactListScreen",
        selfInitializing = true,
        showInSideDrawer = true,
    ),

    Settings(
        titleRes = R.string.screen_settings,
        icon = Icons.Default.Settings,
        key = "SettingsScreen",
        selfInitializing = true,
        showInSideDrawer = true,
    ),

    ImportExport(
        titleRes = R.string.screen_import_export,
        icon = Icons.Default.Output,
        key = "ImportExportScreen",
        selfInitializing = true,
        showInSideDrawer = true,
    ),

    Introduction(
        titleRes = R.string.screen_introduction,
        icon = Icons.Default.Lightbulb,
        key = "Introduction",
        selfInitializing = true,
        showInSideDrawer = true,
    ),

    AboutTheApp(
        titleRes = R.string.screen_about_the_app,
        icon = Icons.Default.Info,
        key = "AboutTheAppScreen",
        selfInitializing = true,
        showInSideDrawer = true,
    ),

    ContactDetail(
        titleRes = R.string.screen_contact_details,
        icon = Icons.Default.ContactPage,
        key = "ContactDetailScreen",
        selfInitializing = false,
        showInSideDrawer = false,
    ),

    ContactEdit(
        titleRes = R.string.screen_contact_edit,
        icon = Icons.Default.ContactPage,
        key = "ContactEditScreen",
        selfInitializing = false,
        showInSideDrawer = false,
    );

    companion object {
        val sideDrawerScreens: List<Screen> =
            entries.filter { it.showInSideDrawer && it.selfInitializing }
    }
}
