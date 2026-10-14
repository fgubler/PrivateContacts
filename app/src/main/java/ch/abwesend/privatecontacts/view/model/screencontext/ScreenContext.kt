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
    override fun navigateUp() {
        navigationViewModel.navigateUp()
    }

    /** from [IScreenContextWithGenericNavigation] */
    override fun navigateToSelfInitializingScreen(screen: Screen) {
        navigationViewModel.navigateTo(screen)
    }

    /** from [IContactListScreenContext] */
    override fun navigateToContactDetailScreen(contact: IContactBase) =
        navigationViewModel.navigateToContactDetail(contact.id)

    /** from [IContactListScreenContext] */
    override fun navigateToContactCreateScreen() =
        navigationViewModel.navigateToContactEdit(ContactEditTarget.New)

    /** from [IContactDetailScreenContext] */
    override fun navigateToContactEditScreen(contactId: ContactId) =
        navigationViewModel.navigateToContactEdit(ContactEditTarget.Existing(contactId))

    /** from [IContactEditScreenContext] */
    override fun returnToContactDetailScreen(contactId: ContactId?) {
        navigationViewModel.returnToContactDetail(contactId)
    }

    /** from [IScreenContext] */
    override fun navigateToUnsavedContactEditScreen(contact: IContact) =
        navigationViewModel.navigateToContactEdit(ContactEditTarget.Unsaved(contact))

    /** from [IContactImportExportScreenContext] */
    override fun navigateToSettingsScreen() {
        navigationViewModel.navigateTo(Screen.Settings)
    }

    override fun refreshSettingsScreen() = navigationViewModel.refreshCurrentScreen()
}
