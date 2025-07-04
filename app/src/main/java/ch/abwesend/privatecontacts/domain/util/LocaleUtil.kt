/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.util

import android.app.LocaleManager
import android.content.Context
import android.os.Build
import android.os.LocaleList.forLanguageTags
import androidx.core.content.getSystemService
import ch.abwesend.privatecontacts.domain.settings.AppLanguage

object LocaleUtil {
    fun tryApplyLanguage(context: Context, language: AppLanguage) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.getSystemService<LocaleManager>()?.let { localeManager ->
                val newLocaleList = if (language.locale == null) {
                    localeManager.systemLocales
                } else { forLanguageTags(language.locale.language) }
                localeManager.applicationLocales = newLocaleList
            }
        }
    }
}