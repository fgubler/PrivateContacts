/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.contact

import androidx.annotation.StringRes
import ch.abwesend.privatecontacts.R

enum class ContactType(@StringRes label: Int) {
    PRIVATE(R.string.private_contact),
    PUBLIC(R.string.public_contact),
}
