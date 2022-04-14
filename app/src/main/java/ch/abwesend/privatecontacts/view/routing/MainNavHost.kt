/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.routing

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import ch.abwesend.privatecontacts.view.model.ScreenContext
import ch.abwesend.privatecontacts.view.routing.Screen.AboutTheApp
import ch.abwesend.privatecontacts.view.routing.Screen.ContactDetail
import ch.abwesend.privatecontacts.view.routing.Screen.ContactEdit
import ch.abwesend.privatecontacts.view.routing.Screen.ContactList
import ch.abwesend.privatecontacts.view.routing.Screen.Introduction
import ch.abwesend.privatecontacts.view.screens.about.AboutScreen
import ch.abwesend.privatecontacts.view.screens.contactdetail.ContactDetailScreen
import ch.abwesend.privatecontacts.view.screens.contactedit.ContactEditScreen
import ch.abwesend.privatecontacts.view.screens.contactlist.ContactListScreen
import ch.abwesend.privatecontacts.view.screens.introduction.IntroductionScreen
import kotlinx.coroutines.FlowPreview

@FlowPreview
@ExperimentalComposeUiApi
@ExperimentalMaterialApi
@Composable
fun MainNavHost(navController: NavHostController, screenContext: ScreenContext) {
    NavHost(navController = navController, startDestination = ContactList.key) {
        composable(ContactList.key) { ContactListScreen.Screen(screenContext) }
        composable(ContactDetail.key) { ContactDetailScreen.Screen(screenContext) }
        composable(ContactEdit.key) { ContactEditScreen.Screen(screenContext) }
        composable(Introduction.key) { IntroductionScreen.Screen(screenContext) }
        composable(AboutTheApp.key) { AboutScreen.Screen(screenContext) }
    }
}
