/*
 * Private Contacts
 * Copyright (c) 2024.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.backup

import androidx.annotation.StringRes
import ch.abwesend.privatecontacts.R

enum class BackupFrequency(@StringRes val label: Int) {
    DISABLED(R.string.backup_frequency_disabled),
    DAILY(R.string.backup_frequency_daily),
    WEEKLY(R.string.backup_frequency_weekly),
    MONTHLY(R.string.backup_frequency_monthly);

    companion object {
        val default = DISABLED
    }
}