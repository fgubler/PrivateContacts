package ch.abwesend.privatecontacts.view.routing

import android.widget.Toast
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import ch.abwesend.privatecontacts.domain.lib.logging.logger

class AppRouter(private val navController: NavHostController) {
    fun navigateToScreen(screen: Screen, navOptions: NavOptions? = null): Boolean =
        when (screen) {
            Screen.ContactList -> tryNavigate(screen, navOptions)
            Screen.Settings -> notYetImplemented()  // TODO implement
            Screen.ContactDevelopers -> notYetImplemented()  // TODO implement
            Screen.ContactDetail -> notYetImplemented()  // TODO implement
        }

    private fun notYetImplemented(): Boolean {
        Toast
            .makeText(navController.context, "Screen not yet implemented", Toast.LENGTH_SHORT)
            .show()
        return false
    }

    private fun tryNavigate(screen: Screen, navOptions: NavOptions? = null): Boolean =
        try {
            navController.navigate(screen.key)
            true
        } catch(e: IllegalArgumentException) {
            e.logger.warning("Cannot navigate to '${screen.key}': screen not found")
            false
        }
}
