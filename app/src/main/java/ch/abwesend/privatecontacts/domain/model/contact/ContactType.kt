/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.contact

import androidx.annotation.StringRes
import ch.abwesend.privatecontacts.R

enum class ContactType(@StringRes val label: Int) {
    SECRET(R.string.secret_contact),
    PUBLIC(R.string.public_contact),
}
