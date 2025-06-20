/*
 * Private Contacts
 * Copyright (c) 2025.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.appearance

import ch.abwesend.privatecontacts.R

/** Enum to describe the behavior of the second tab (not containing only the secret contacts). */
enum class SecondTabMode(val labelRes: Int) {
    ALL_CONTACTS(R.string.all_contacts_tab_title),
    PUBLIC_CONTACTS(R.string.public_contacts_tab_title),
}
