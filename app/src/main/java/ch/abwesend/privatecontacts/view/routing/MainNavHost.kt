package ch.abwesend.privatecontacts.view.routing

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import ch.abwesend.privatecontacts.view.model.ScreenContext
import ch.abwesend.privatecontacts.view.routing.Screen.ContactEdit
import ch.abwesend.privatecontacts.view.routing.Screen.ContactList
import ch.abwesend.privatecontacts.view.screens.contactedit.ContactEditScreen
import ch.abwesend.privatecontacts.view.screens.contactedit.Screen
import ch.abwesend.privatecontacts.view.screens.contactlist.ContactListScreen
import ch.abwesend.privatecontacts.view.screens.contactlist.Screen

@ExperimentalMaterialApi
@Composable
fun MainNavHost(navController: NavHostController, screenContext: ScreenContext) {
    NavHost(navController = navController, startDestination = ContactList.key) {
        composable(ContactList.key) { ContactListScreen.Screen(screenContext) }
        composable(ContactEdit.key) { ContactEditScreen.Screen(screenContext) }
    }
}
