package ch.abwesend.privatecontacts.view.routing

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import ch.abwesend.privatecontacts.R

sealed interface Screen {
    val titleRes: Int
    val icon: ImageVector
    val key: String

    object ContactList : Screen {
        @StringRes override val titleRes: Int = R.string.screen_contact_list
        override val icon: ImageVector = Icons.Default.Contacts
        override val key: String = "ContactListScreen"
    }
    object Settings : Screen {
        @StringRes override val titleRes: Int = R.string.screen_settings
        override val icon: ImageVector = Icons.Default.Settings
        override val key: String = "SettingsScreen"
    }
    object Contact : Screen {
        @StringRes override val titleRes: Int = R.string.screen_contact
        override val icon: ImageVector = Icons.Default.Email
        override val key: String = "ContactScreen"
    }
}

val sideDrawerScreens: List<Screen>
    get() = listOf(
        Screen.ContactList,
        Screen.Settings,
        Screen.Contact,
    )
