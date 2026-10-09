/*
 * Private Contacts
 * Copyright (c) 2026.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.screens.contactgroup

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.model.contact.ContactType

enum class ContactGroupListTab(
    val index: Int, // ascending
    @param:StringRes val label: Int,
    val icon: ImageVector,
) {
    SECRET_GROUPS(
        index = 0,
        label = R.string.secret_contact_groups_tab_title,
        icon = ContactType.SECRET.icon,
    ),
    PUBLIC_GROUPS(
        index = 1,
        label = R.string.public_contact_groups_tab_title,
        icon = ContactType.PUBLIC.icon,
    ),
    ;

    companion object {
        val valuesSorted: List<ContactGroupListTab> by lazy { entries.sortedBy { it.index } }
        val default: ContactGroupListTab by lazy { valuesSorted.first() }
    }
}
