/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.screens.contactlist

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.model.appearance.SecondTabMode
import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.settings.ISettingsState

enum class ContactListTab(
    val index: Int, // ascending
    @StringRes val label: Int,
    val icon: ImageVector,
    val showContactTypeIcons: Boolean,
    val requiresPermission: Boolean,
    val isVisible: (ISettingsState) -> Boolean,
) {
    SECRET_CONTACTS(
        index = 0,
        label = R.string.secret_contacts_tab_title,
        icon = ContactType.SECRET.icon,
        showContactTypeIcons = false,
        requiresPermission = false,
        isVisible = { true }
    ),
    PUBLIC_CONTACTS(
        index = 1,
        label = R.string.public_contacts_tab_title,
        icon = ContactType.PUBLIC.icon,
        showContactTypeIcons = true,
        requiresPermission = true,
        isVisible = { settings -> settings.secondTabMode == SecondTabMode.PUBLIC_CONTACTS }
    ),
    ALL_CONTACTS(
        index = 1, // same index as public-contacts because always only one of them is visible
        label = R.string.all_contacts_tab_title,
        icon = ContactType.PUBLIC.icon,
        showContactTypeIcons = true,
        requiresPermission = true,
        isVisible = { settings -> settings.secondTabMode == SecondTabMode.ALL_CONTACTS }
    ),
    ;

    companion object {
        val valuesSorted: List<ContactListTab> by lazy { entries.toList().sortedBy { it.index } }
        val default: ContactListTab by lazy { valuesSorted.first() }
    }
}
