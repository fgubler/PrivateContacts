/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.model

import ch.abwesend.privatecontacts.domain.settings.ISettingsState
import ch.abwesend.privatecontacts.view.routing.AppRouter
import ch.abwesend.privatecontacts.view.viewmodel.ContactDetailViewModel
import ch.abwesend.privatecontacts.view.viewmodel.ContactEditViewModel
import ch.abwesend.privatecontacts.view.viewmodel.ContactListViewModel
import ch.abwesend.privatecontacts.view.viewmodel.SettingsViewModel

data class ScreenContext(
    val router: AppRouter,
    val contactListViewModel: ContactListViewModel,
    val contactDetailViewModel: ContactDetailViewModel,
    val contactEditViewModel: ContactEditViewModel,
    val settingsViewModel: SettingsViewModel,
    val settings: ISettingsState,
)
