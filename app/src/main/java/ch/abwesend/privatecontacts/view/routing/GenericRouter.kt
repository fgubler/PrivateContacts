/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.routing

import ch.abwesend.privatecontacts.domain.model.contact.ContactId

class GenericRouter(private val navigationViewModel: NavigationViewModel) {
    fun navigateToScreen(screen: Screen): Boolean = navigationViewModel.navigateTo(screen)

    fun navigateToContactDetail(contactId: ContactId): Boolean {
        navigationViewModel.navigateToContactDetail(contactId)
        return true
    }

    fun navigateToContactEdit(target: ContactEditTarget): Boolean {
        navigationViewModel.navigateToContactEdit(target)
        return true
    }

    fun navigateUp(): Boolean = navigationViewModel.navigateUp()

    fun returnToContactDetail(contactId: ContactId?): Boolean =
        navigationViewModel.returnToContactDetail(contactId)

    fun refreshCurrentScreen() {
        navigationViewModel.refreshCurrentScreen()
    }
}
