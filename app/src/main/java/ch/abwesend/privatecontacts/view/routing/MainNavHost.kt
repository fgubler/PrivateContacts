package ch.abwesend.privatecontacts.view.routing

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import ch.abwesend.privatecontacts.view.model.ScreenContext
import ch.abwesend.privatecontacts.view.routing.Screen.ContactEdit
import ch.abwesend.privatecontacts.view.routing.Screen.ContactList
import ch.abwesend.privatecontacts.view.screens.contactedit.ContactEditScreen
import ch.abwesend.privatecontacts.view.screens.contactlist.ContactListScreen

@Composable
fun MainNavHost(navController: NavHostController, screenContext: ScreenContext) {
    NavHost(navController = navController, startDestination = ContactList.key) {
        composable(ContactList.key) { ContactListScreen(screenContext) }
        composable(ContactEdit.key) { ContactEditScreen(screenContext) }
    }
}
