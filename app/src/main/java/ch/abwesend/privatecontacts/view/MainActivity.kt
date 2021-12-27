package ch.abwesend.privatecontacts.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.navigation.compose.rememberNavController
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.view.routing.MainNavHost
import ch.abwesend.privatecontacts.view.theme.PrivateContactsTheme
import ch.abwesend.privatecontacts.view.viewmodel.ContactListViewModel

class MainActivity : ComponentActivity() {
    private val contactListViewModel: ContactListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logger.info("Main activity started")

        setContent {
            PrivateContactsTheme {
                val navController = rememberNavController()

                MainNavHost(
                    navController = navController,
                    contactListViewModel = contactListViewModel,
                )
            }
        }
    }
}
