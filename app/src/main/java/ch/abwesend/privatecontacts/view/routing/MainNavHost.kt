package ch.abwesend.privatecontacts.view.routing

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import ch.abwesend.privatecontacts.view.model.ScreenContext
import ch.abwesend.privatecontacts.view.screens.contactlist.ContactListScreen
import ch.abwesend.privatecontacts.view.viewmodel.ContactListViewModel
import org.koin.androidx.compose.get
import org.koin.core.parameter.parametersOf

@Composable
fun MainNavHost(navController: NavHostController, contactListViewModel: ContactListViewModel) {
    val router: AppRouter = get { parametersOf(navController) }
    val screenContext = ScreenContext(
        router = router,
        contactListViewModel = contactListViewModel,
    )

    NavHost(navController = navController, startDestination = Screen.ContactList.key) {
        composable(Screen.ContactList.key) { ContactListScreen(screenContext) }
    }
}
