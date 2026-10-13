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
import ch.abwesend.privatecontacts.view.routing.GenericRouter
import ch.abwesend.privatecontacts.view.routing.Screen
import ch.abwesend.privatecontacts.view.screens.contactdetail.ContactDetailViewModel
import ch.abwesend.privatecontacts.view.screens.contactedit.ContactEditViewModel
import ch.abwesend.privatecontacts.view.screens.contactgroup.ContactGroupListViewModel
import ch.abwesend.privatecontacts.view.screens.contactlist.ContactListViewModel
import ch.abwesend.privatecontacts.view.screens.importexport.export.ContactExportViewModel
import ch.abwesend.privatecontacts.view.screens.importexport.import.ContactImportViewModel
import ch.abwesend.privatecontacts.view.screens.settings.SettingsViewModel

data class ScreenContext(
    private val genericRouter: GenericRouter,
    override val contactListViewModel: ContactListViewModel,
    override val contactGroupListViewModel: ContactGroupListViewModel,
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
        genericRouter.navigateToScreen(screen)

    /** from [IContactListScreenContext] */
    override fun navigateToContactDetailScreen(contact: IContactBase): Boolean =
        genericRouter.navigateToContactDetail(contact.id)

    /** from [IContactListScreenContext] */
    override fun navigateToContactCreateScreen(): Boolean =
        genericRouter.navigateToContactEdit(ContactEditTarget.New)

    /** from [IContactDetailScreenContext] */
    override fun navigateToContactEditScreen(contactId: ContactId): Boolean =
        genericRouter.navigateToContactEdit(ContactEditTarget.Existing(contactId))

    /** from [IContactEditScreenContext] */
    override fun returnToContactDetailScreen(contactId: ContactId?): Boolean =
        genericRouter.returnToContactDetail(contactId)

    /** from [IScreenContext] */
    override fun navigateToUnsavedContactEditScreen(contact: IContact): Boolean =
        genericRouter.navigateToContactEdit(ContactEditTarget.Unsaved(contact))

    /** from [IContactImportExportScreenContext] */
    override fun navigateToSettingsScreen(): Boolean =
        genericRouter.navigateToScreen(Screen.Settings)

    override fun refreshSettingsScreen() = genericRouter.refreshCurrentScreen()
}
