/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.routing

class GenericRouter(private val navigationViewModel: NavigationViewModel) {
    fun navigateToScreen(screen: Screen): Boolean {
        navigationViewModel.navigateTo(screen)
        return true
    }

    fun navigateUp(): Boolean = navigationViewModel.navigateUp()

    fun refreshCurrentScreen() {
        navigationViewModel.refreshCurrentScreen()
    }
}
