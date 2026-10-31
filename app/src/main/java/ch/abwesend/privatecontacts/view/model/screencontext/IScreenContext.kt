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
import ch.abwesend.privatecontacts.view.routing.Screen

/**
 * The idea of these interfaces is to improve information-hiding and deliver to each screen
 * only the information and navigation-capabilities which it actually needs.
 */
interface IScreenContextBase {
    val settings: ISettingsState
    val permissionProvider: IPermissionProvider
    fun navigateUp()
}

interface IScreenContextWithGenericNavigation : IScreenContextBase {
    fun navigateToSelfInitializingScreen(screen: Screen)
}

interface IContactListScreenContext : IScreenContextWithGenericNavigation {
    fun navigateToContactDetailScreen(contact: IContactBase)
    fun navigateToContactCreateScreen()
}

interface IContactGroupListScreenContext : IScreenContextBase

interface IContactDetailScreenContext : IScreenContextBase {
    fun navigateToContactEditScreen(contactId: ContactId)
}

interface IContactEditScreenContext : IScreenContextBase {
    fun returnToContactDetailScreen(contactId: ContactId? = null)
}

interface ISettingsScreenContext : IScreenContextBase {
    fun refreshSettingsScreen()
}

interface IContactImportExportScreenContext : IScreenContextBase {
    fun navigateToSettingsScreen()
}

interface IScreenContext :
    IContactListScreenContext,
    IContactGroupListScreenContext,
    IContactDetailScreenContext,
    IContactEditScreenContext,
    ISettingsScreenContext,
    IContactImportExportScreenContext {
    /** e.g. for a contact parsed from a vcf-file which was opened with the app */
    fun navigateToUnsavedContactEditScreen(contact: IContact)
}
