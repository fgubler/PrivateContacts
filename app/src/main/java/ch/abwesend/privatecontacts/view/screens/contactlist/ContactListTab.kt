/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.screens.contactlist

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.ui.graphics.vector.ImageVector
import ch.abwesend.privatecontacts.R

enum class ContactListTab(
    val index: Int, // ascending
    @StringRes val label: Int,
    val icon: ImageVector,
) {
    SECRET_CONTACTS(index = 0, label = R.string.secret_contacts_tab_title, icon = Icons.Default.Lock),
    ALL_CONTACTS(index = 1, label = R.string.all_contacts_tab_title, icon = Icons.Default.LockOpen),
    ;

    companion object {
        val valuesSorted: List<ContactListTab> by lazy { values().toList().sortedBy { it.index } }
        val default: ContactListTab by lazy { valuesSorted.first() }
    }
}
