/*
 * Private Contacts
 * Copyright (c) 2026.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.importexport

import androidx.annotation.StringRes
import ch.abwesend.privatecontacts.R

enum class BackupFrequency(@param:StringRes val label: Int) {
    DISABLED(label = R.string.backup_frequency_disabled),
    DAILY(label = R.string.backup_frequency_daily),
    WEEKLY(label = R.string.backup_frequency_weekly),
    MONTHLY(label = R.string.backup_frequency_monthly);

    companion object {
        val default: BackupFrequency = DISABLED
    }
}
