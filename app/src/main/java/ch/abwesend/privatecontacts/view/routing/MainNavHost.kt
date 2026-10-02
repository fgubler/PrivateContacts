/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.routing

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.scene.Scene
import androidx.navigation3.ui.NavDisplay
import ch.abwesend.privatecontacts.view.model.screencontext.ScreenContext
import ch.abwesend.privatecontacts.view.routing.Screen.AboutTheApp
import ch.abwesend.privatecontacts.view.routing.Screen.ContactDetail
import ch.abwesend.privatecontacts.view.routing.Screen.ContactEdit
import ch.abwesend.privatecontacts.view.routing.Screen.ContactList
import ch.abwesend.privatecontacts.view.routing.Screen.ImportExport
import ch.abwesend.privatecontacts.view.routing.Screen.Introduction
import ch.abwesend.privatecontacts.view.routing.Screen.Settings
import ch.abwesend.privatecontacts.view.screens.about.AboutScreen
import ch.abwesend.privatecontacts.view.screens.contactdetail.ContactDetailScreen
import ch.abwesend.privatecontacts.view.screens.contactedit.ContactEditScreen
import ch.abwesend.privatecontacts.view.screens.contactlist.ContactListScreen
import ch.abwesend.privatecontacts.view.screens.importexport.ContactImportExportScreen
import ch.abwesend.privatecontacts.view.screens.introduction.IntroductionScreen
import ch.abwesend.privatecontacts.view.screens.settings.SettingsScreen
import kotlinx.coroutines.FlowPreview
import kotlin.contracts.ExperimentalContracts

/** the duration used by the old Jetpack-Navigation NavHost: keep the visual style unchanged */
private const val TRANSITION_DURATION_MILLIS = 700

@ExperimentalFoundationApi
@FlowPreview
@ExperimentalContracts
@Composable
fun MainNavHost(navigationViewModel: NavigationViewModel, screenContext: ScreenContext) {
    val transitionSpec: AnimatedContentTransitionScope<Scene<ScreenInstance>>.() -> ContentTransform = {
        fadeIn(animationSpec = tween(TRANSITION_DURATION_MILLIS)) togetherWith
            fadeOut(animationSpec = tween(TRANSITION_DURATION_MILLIS))
    }

    NavDisplay(
        backStack = navigationViewModel.backStack,
        onBack = { navigationViewModel.navigateUp() },
        transitionSpec = transitionSpec,
        popTransitionSpec = transitionSpec,
        predictivePopTransitionSpec = { transitionSpec() },
        entryProvider = { screenInstance ->
            NavEntry(
                key = screenInstance,
                contentKey = screenInstance.contentKey,
            ) { selectedInstance ->
                when (selectedInstance.screen) {
                    ContactList -> ContactListScreen.Screen(screenContext)
                    ContactDetail -> ContactDetailScreen.Screen(screenContext)
                    ContactEdit -> ContactEditScreen.Screen(screenContext)
                    Settings -> SettingsScreen.Screen(screenContext)
                    ImportExport -> ContactImportExportScreen.Screen(screenContext)
                    Introduction -> IntroductionScreen.Screen(screenContext)
                    AboutTheApp -> AboutScreen.Screen(screenContext)
                }
            }
        },
    )
}
