package ch.abwesend.privatecontacts.view.util

import android.app.LocaleManager
import android.content.Context
import android.os.Build
import android.os.LocaleList
import androidx.core.content.getSystemService
import ch.abwesend.privatecontacts.domain.settings.AppLanguage

fun Context.tryChangeAppLanguage(language: AppLanguage) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getSystemService<LocaleManager>()?.let { localeManager ->
            val newLocaleList = if (language.locale == null) {
                localeManager.systemLocales
            } else {
                LocaleList.forLanguageTags(language.locale.language)
            }
            localeManager.applicationLocales = newLocaleList
        }
    }
}
