/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.model.screencontext

import androidx.navigation.NavOptions
import ch.abwesend.privatecontacts.domain.ContactDetailInitializationWorkaround
import ch.abwesend.privatecontacts.domain.lib.logging.error
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.contact.IContact
import ch.abwesend.privatecontacts.domain.model.contact.IContactBase
import ch.abwesend.privatecontacts.domain.settings.ISettingsState
import ch.abwesend.privatecontacts.view.permission.IPermissionProvider
import ch.abwesend.privatecontacts.view.routing.GenericRouter
import ch.abwesend.privatecontacts.view.routing.Screen
import ch.abwesend.privatecontacts.view.viewmodel.ContactDetailViewModel
import ch.abwesend.privatecontacts.view.viewmodel.ContactEditViewModel
import ch.abwesend.privatecontacts.view.viewmodel.ContactExportViewModel
import ch.abwesend.privatecontacts.view.viewmodel.ContactImportViewModel
import ch.abwesend.privatecontacts.view.viewmodel.ContactListViewModel
import ch.abwesend.privatecontacts.view.viewmodel.SettingsViewModel

data class ScreenContext(
    private val genericRouter: GenericRouter,
    override val contactListViewModel: ContactListViewModel,
    override val contactDetailViewModel: ContactDetailViewModel,
    override val contactEditViewModel: ContactEditViewModel,
    override val settingsViewModel: SettingsViewModel,
    override val exportViewModel: ContactExportViewModel,
    override val importViewModel: ContactImportViewModel,
    override val permissionProvider: IPermissionProvider,
    override val settings: ISettingsState,
) : IScreenContext {
    /** from [IScreenContextBase] */
    override fun navigateUp() = genericRouter.navigateUp()

    /** from [IScreenContextWithGenericNavigation] */
    override fun navigateToSelfInitializingScreen(screen: Screen): Boolean =
        if (screen.selfInitializing) {
            genericRouter.navigateToScreen(screen)
        } else {
            logger.error("Trying to navigate to screen '$screen' without passing data")
            false
        }

    /** from [IContactListScreenContext] */
    override fun navigateToContactDetailScreen(contact: IContactBase): Boolean {
        ContactDetailInitializationWorkaround.hasOpenedContact = true
        contactDetailViewModel.selectContact(contact)
        return genericRouter.navigateToScreen(Screen.ContactDetail)
    }

    /** from [IContactListScreenContext] */
    override fun navigateToContactCreateScreen(): Boolean {
        contactEditViewModel.createContact()
        return genericRouter.navigateToScreen(Screen.ContactEdit)
    }

    /** from [IContactDetailScreenContext] */
    override fun navigateToContactEditScreen(contact: IContact): Boolean {
        contactEditViewModel.selectContact(contact)
        return genericRouter.navigateToScreen(Screen.ContactEdit)
    }

    /** from [IContactEditScreenContext] */
    override fun returnToContactDetailScreen(contact: IContactBase?): Boolean {
        if (contact == null) {
            contactDetailViewModel.reloadContact()
        } else {
            contactDetailViewModel.selectContact(contact)
        }
        return genericRouter.navigateUp()
    }

    override fun refreshSettingsScreen(): Boolean {
        val navOptions = NavOptions.Builder()
            .setPopUpTo(route = Screen.Settings.key, inclusive = true, saveState = false)
            .build()
        return genericRouter.navigateToScreen(Screen.Settings, navOptions)
    }
}
