/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.settings

import androidx.annotation.StringRes
import ch.abwesend.privatecontacts.R

enum class AppTheme(@StringRes val labelRes: Int) {
    LIGHT_MODE(R.string.light_theme),
    DARK_MODE(R.string.dark_theme),
    SYSTEM_SETTINGS(R.string.system_default),
}
