/*
 * Private Contacts
 * Copyright (c) 2024.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.backup

import androidx.annotation.StringRes
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.model.contact.ContactType

enum class BackupScope(@StringRes val label: Int) {
    ALL(R.string.backup_scope_all),
    SECRET_ONLY(R.string.backup_scope_secret),
    PUBLIC_ONLY(R.string.backup_scope_public);

    fun toContactType(): ContactType? = when (this) {
        SECRET_ONLY -> ContactType.SECRET
        PUBLIC_ONLY -> ContactType.PUBLIC
        ALL -> null // null means all types
    }

    companion object {
        val default = ALL
    }
}