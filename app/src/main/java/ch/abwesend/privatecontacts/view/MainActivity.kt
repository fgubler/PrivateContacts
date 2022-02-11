package ch.abwesend.privatecontacts.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.util.getAnywhereWithParams
import ch.abwesend.privatecontacts.view.model.ScreenContext
import ch.abwesend.privatecontacts.view.routing.AppRouter
import ch.abwesend.privatecontacts.view.routing.MainNavHost
import ch.abwesend.privatecontacts.view.theme.PrivateContactsTheme
import ch.abwesend.privatecontacts.view.viewmodel.ContactEditViewModel
import ch.abwesend.privatecontacts.view.viewmodel.ContactListViewModel

@ExperimentalComposeUiApi
@ExperimentalMaterialApi
class MainActivity : ComponentActivity() {
    private val contactListViewModel: ContactListViewModel by viewModels()
    private val contactEditViewModel: ContactEditViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logger.info("Main activity started")

        setContent {
            PrivateContactsTheme {
                val navController = rememberNavController()
                val screenContext = createScreenContext(navController)

                MainNavHost(
                    navController = navController,
                    screenContext = screenContext,
                )
            }
        }
    }

    private fun createScreenContext(navController: NavHostController): ScreenContext {
        val router: AppRouter = getAnywhereWithParams(navController)

        return ScreenContext(
            router = router,
            contactListViewModel = contactListViewModel,
            contactEditViewModel = contactEditViewModel,
        )
    }
}
