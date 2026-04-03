/*
 * Private Contacts
 * Copyright (c) 2026.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.importexport

import androidx.annotation.StringRes
import ch.abwesend.privatecontacts.R

enum class NumberOfBackupsToKeep(@param:StringRes val label: Int, val maxCount: Int) {
    ALL(label = R.string.backup_number_to_keep_all, maxCount = Int.MAX_VALUE),
    FIVE(label = R.string.backup_number_to_keep_5, maxCount = 5),
    TEN(label = R.string.backup_number_to_keep_10, maxCount = 10),
    THIRTY(label = R.string.backup_number_to_keep_30, maxCount = 30),
    FIFTY(label = R.string.backup_number_to_keep_50, maxCount = 50);

    companion object {
        val default: NumberOfBackupsToKeep = ALL
    }
}
