/*
 * Private Contacts
 * Copyright (c) 2024.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.backup

import androidx.annotation.StringRes
import ch.abwesend.privatecontacts.R

enum class BackupScope(@StringRes val label: Int) {
    ALL(R.string.backup_scope_all),
    SECRET_ONLY(R.string.backup_scope_secret),
    PUBLIC_ONLY(R.string.backup_scope_public);

    companion object {
        val default = ALL
    }
}