package ch.abwesend.privatecontacts.view.routing

import androidx.navigation.NavHostController
import androidx.navigation.NavOptions

class AppRouter(private val navController: NavHostController) {

    fun navigateToScreen(screen: Screen, navOptions: NavOptions? = null): Boolean =
        when (screen) {
            Screen.ContactList -> navigateToContactListScreen()
            Screen.Settings -> navigateToSettingsScreen()
            Screen.Contact -> navigateToContactScreen()
        }

    private fun navigateToContactListScreen(): Boolean {
        navController.navigate(Screen.ContactList.key)
        return true
    }
    private fun navigateToSettingsScreen(): Boolean {
        // TODO implement
        return false
    }
    private fun navigateToContactScreen(): Boolean {
        // TODO implement
        return false
    }
}
