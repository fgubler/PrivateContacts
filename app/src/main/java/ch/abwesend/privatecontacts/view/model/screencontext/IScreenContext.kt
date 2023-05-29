/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.model.screencontext

import ch.abwesend.privatecontacts.domain.model.contact.IContact
import ch.abwesend.privatecontacts.domain.model.contact.IContactBase
import ch.abwesend.privatecontacts.domain.settings.ISettingsState
import ch.abwesend.privatecontacts.view.routing.Screen
import ch.abwesend.privatecontacts.view.viewmodel.ContactDetailViewModel
import ch.abwesend.privatecontacts.view.viewmodel.ContactEditViewModel
import ch.abwesend.privatecontacts.view.viewmodel.ContactExportViewModel
import ch.abwesend.privatecontacts.view.viewmodel.ContactImportViewModel
import ch.abwesend.privatecontacts.view.viewmodel.ContactListViewModel
import ch.abwesend.privatecontacts.view.viewmodel.SettingsViewModel

/**
 * The idea of these interfaces is to improve information-hiding and deliver to each screen
 * only the information and navigation-capabilities which it actually needs.
 */
interface IScreenContextBase {
    val settings: ISettingsState
    fun navigateUp(): Boolean
}

interface IScreenContextWithGenericNavigation : IScreenContextBase {
    fun navigateToSelfInitializingScreen(screen: Screen): Boolean
}

interface IContactListScreenContext : IScreenContextWithGenericNavigation {
    val contactListViewModel: ContactListViewModel

    fun navigateToContactDetailScreen(contact: IContactBase): Boolean
    fun navigateToContactCreateScreen(): Boolean
}

interface IContactDetailScreenContext : IScreenContextBase {
    val contactDetailViewModel: ContactDetailViewModel
    fun navigateToContactEditScreen(contact: IContact): Boolean
}

interface IContactEditScreenContext : IScreenContextBase {
    val contactEditViewModel: ContactEditViewModel
    fun returnToContactDetailScreen(): Boolean
}

interface ISettingsScreenContext : IScreenContextBase {
    val settingsViewModel: SettingsViewModel
}

interface IContactImportExportScreenContext : IScreenContextBase {
    val exportViewModel: ContactExportViewModel
    val importViewModel: ContactImportViewModel
}

interface IScreenContext :
    IContactListScreenContext,
    IContactDetailScreenContext,
    IContactEditScreenContext,
    ISettingsScreenContext,
    IContactImportExportScreenContext
