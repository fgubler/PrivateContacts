/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.model.screencontext

import ch.abwesend.privatecontacts.domain.lib.logging.error
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.contact.IContact
import ch.abwesend.privatecontacts.domain.model.contact.IContactBase
import ch.abwesend.privatecontacts.domain.settings.ISettingsState
import ch.abwesend.privatecontacts.view.routing.GenericRouter
import ch.abwesend.privatecontacts.view.routing.Screen
import ch.abwesend.privatecontacts.view.viewmodel.ContactDetailViewModel
import ch.abwesend.privatecontacts.view.viewmodel.ContactEditViewModel
import ch.abwesend.privatecontacts.view.viewmodel.ContactListViewModel
import ch.abwesend.privatecontacts.view.viewmodel.ExportViewModel
import ch.abwesend.privatecontacts.view.viewmodel.ImportViewModel
import ch.abwesend.privatecontacts.view.viewmodel.SettingsViewModel

data class ScreenContext(
    private val genericRouter: GenericRouter,
    override val contactListViewModel: ContactListViewModel,
    override val contactDetailViewModel: ContactDetailViewModel,
    override val contactEditViewModel: ContactEditViewModel,
    override val settingsViewModel: SettingsViewModel,
    override val exportViewModel: ExportViewModel,
    override val importViewModel: ImportViewModel,
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
    override fun returnToContactDetailScreen(): Boolean {
        contactDetailViewModel.reloadContact()
        return genericRouter.navigateUp()
    }
}
