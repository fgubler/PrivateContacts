/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.model.screencontext

import ch.abwesend.privatecontacts.domain.model.contact.ContactId
import ch.abwesend.privatecontacts.domain.model.contact.IContact
import ch.abwesend.privatecontacts.domain.model.contact.IContactBase
import ch.abwesend.privatecontacts.domain.settings.ISettingsState
import ch.abwesend.privatecontacts.view.permission.IPermissionProvider
import ch.abwesend.privatecontacts.view.routing.ContactEditTarget
import ch.abwesend.privatecontacts.view.routing.NavigationViewModel
import ch.abwesend.privatecontacts.view.routing.Screen

data class ScreenContext(
    private val navigationViewModel: NavigationViewModel,
    override val permissionProvider: IPermissionProvider,
    override val settings: ISettingsState,
) : IScreenContext {
    /** from [IScreenContextBase] */
    override fun navigateUp() = navigationViewModel.navigateUp()

    /** from [IScreenContextWithGenericNavigation] */
    override fun navigateToSelfInitializingScreen(screen: Screen): Boolean =
        navigationViewModel.navigateTo(screen)

    /** from [IContactListScreenContext] */
    override fun navigateToContactDetailScreen(contact: IContactBase): Boolean {
        navigationViewModel.navigateToContactDetail(contact.id)
        return true
    }

    /** from [IContactListScreenContext] */
    override fun navigateToContactCreateScreen(): Boolean {
        navigationViewModel.navigateToContactEdit(ContactEditTarget.New)
        return true
    }

    /** from [IContactDetailScreenContext] */
    override fun navigateToContactEditScreen(contactId: ContactId): Boolean {
        navigationViewModel.navigateToContactEdit(ContactEditTarget.Existing(contactId))
        return true
    }

    /** from [IContactEditScreenContext] */
    override fun returnToContactDetailScreen(contactId: ContactId?): Boolean =
        navigationViewModel.returnToContactDetail(contactId)

    /** from [IScreenContext] */
    override fun navigateToUnsavedContactEditScreen(contact: IContact): Boolean {
        navigationViewModel.navigateToContactEdit(ContactEditTarget.Unsaved(contact))
        return true
    }

    /** from [IContactImportExportScreenContext] */
    override fun navigateToSettingsScreen(): Boolean =
        navigationViewModel.navigateTo(Screen.Settings)

    override fun refreshSettingsScreen() = navigationViewModel.refreshCurrentScreen()
}
