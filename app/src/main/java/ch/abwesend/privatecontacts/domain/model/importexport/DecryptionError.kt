/*
 * Private Contacts
 * Copyright (c) 2026.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.importexport

import androidx.annotation.StringRes
import ch.abwesend.privatecontacts.R

enum class DecryptionError(@field:StringRes val label: Int) {
    INVALID_PASSWORD(R.string.decryption_failed_invalid_password),
    INVALID_FILE(R.string.decryption_failed_invalid_file),
    UNKNOWN(R.string.decryption_failed),
}
