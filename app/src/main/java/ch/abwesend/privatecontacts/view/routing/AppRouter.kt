package ch.abwesend.privatecontacts.view.routing

import android.widget.Toast
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import ch.abwesend.privatecontacts.domain.lib.logging.logger

class AppRouter(private val navController: NavHostController) {
    fun navigateToScreen(screen: Screen, navOptions: NavOptions? = null): Boolean =
        tryNavigate(screen, navOptions)

    fun navigateUp(): Boolean = navController.navigateUp()

    private fun tryNavigate(screen: Screen, navOptions: NavOptions? = null): Boolean =
        try {
            navController.navigate(screen.key)
            true
        } catch (e: IllegalArgumentException) {
            e.logger.warning("Cannot navigate to '${screen.key}': screen not found")

            Toast
                .makeText(navController.context, "Screen not yet implemented", Toast.LENGTH_SHORT)
                .show()

            false
        }
}
