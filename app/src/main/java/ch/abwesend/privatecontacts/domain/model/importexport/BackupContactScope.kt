/*
 * Private Contacts
 * Copyright (c) 2026.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.importexport

import androidx.annotation.StringRes
import ch.abwesend.privatecontacts.R

enum class BackupContactScope(@param:StringRes val label: Int) {
    ALL(label = R.string.backup_contact_type_all),
    SECRET(label = R.string.backup_contact_type_secret),
    PUBLIC(label = R.string.backup_contact_type_public);

    companion object {
        val default: BackupContactScope = SECRET
    }
}
