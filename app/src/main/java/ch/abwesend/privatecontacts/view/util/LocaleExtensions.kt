package ch.abwesend.privatecontacts.view.util

import android.app.LocaleManager
import android.content.Context
import android.os.Build
import android.os.LocaleList
import androidx.core.content.getSystemService
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.settings.AppLanguage

fun Context.tryChangeAppLanguage(language: AppLanguage) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getSystemService<LocaleManager>()?.let { localeManager ->
            val newLocaleList = language.locale
                ?.let { LocaleList.forLanguageTags(it.language) }
                ?: localeManager.systemLocales

            if (localeManager.applicationLocales != newLocaleList) {
                try {
                    localeManager.applicationLocales = newLocaleList
                } catch (e: IllegalStateException) {
                    logger.warning("Failed to change the application locales", e)
                }
            }
        }
    }
}
