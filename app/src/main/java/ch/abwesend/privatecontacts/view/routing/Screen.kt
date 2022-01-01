package ch.abwesend.privatecontacts.view.routing

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContactPage
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Email
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

    ContactDevelopers(
        titleRes = R.string.screen_contact_developers,
        icon = Icons.Default.Email,
        key = "ContactDevelopersScreen",
        showInSideDrawer = true,
    ),

    ContactDetail(
        titleRes = R.string.screen_contact_details,
        icon = Icons.Default.ContactPage,
        key = "ContactDetailsScreen",
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
