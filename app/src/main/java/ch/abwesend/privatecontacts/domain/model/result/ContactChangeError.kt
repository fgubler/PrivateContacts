/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.result

import androidx.annotation.StringRes
import ch.abwesend.privatecontacts.R

enum class ContactChangeError(@StringRes val label: Int) {
    UNKNOWN_ERROR(R.string.unknown_error),
    NOT_YET_IMPLEMENTED_FOR_EXTERNAL_CONTACTS(R.string.not_yet_implemented_for_external_error)
}
