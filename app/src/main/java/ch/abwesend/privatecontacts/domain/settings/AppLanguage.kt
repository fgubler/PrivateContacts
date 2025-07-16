/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.settings

import androidx.annotation.StringRes
import ch.abwesend.privatecontacts.R
import java.util.Locale

enum class AppLanguage(@StringRes val labelRes: Int, val locale: Locale?) {
    SYSTEM_DEFAULT(R.string.system_default, null),
    ENGLISH(R.string.language_english, Locale.ENGLISH),
    GERMAN(R.string.language_german, Locale.GERMAN),
    SPANISH(R.string.language_spanish, Locale("es")),
    FRENCH(R.string.language_french, Locale.FRENCH),
    ITALIAN(R.string.language_italian, Locale.ITALIAN),
}
