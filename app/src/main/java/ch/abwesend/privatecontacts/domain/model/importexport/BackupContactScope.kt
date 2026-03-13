/*
 * Private Contacts
 * Copyright (c) 2026.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.importexport

import androidx.annotation.StringRes
import ch.abwesend.privatecontacts.R

enum class BackupContactScope(
    @param:StringRes val label: Int,
    val permissionRequired: Boolean,
) {
    ALL(label = R.string.backup_contact_type_all, permissionRequired = true),
    SECRET(label = R.string.backup_contact_type_secret, permissionRequired = false),
    PUBLIC(label = R.string.backup_contact_type_public, permissionRequired = true);

    companion object {
        val default: BackupContactScope = SECRET
    }
}
