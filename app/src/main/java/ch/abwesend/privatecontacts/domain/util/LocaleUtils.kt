/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.util

import android.content.Context
import android.content.res.Configuration
import ch.abwesend.privatecontacts.domain.settings.AppLanguage
import java.util.Locale

object LocaleUtils {
    fun applyLanguage(context: Context, language: AppLanguage) {
        val locale = language.locale ?: Locale.getDefault()
        updateLocale(context, locale)
    }

    private fun updateLocale(context: Context, locale: Locale) {
        Locale.setDefault(locale)
        val resources = context.resources
        val configuration = Configuration(resources.configuration)

        configuration.setLocale(locale)
        context.createConfigurationContext(configuration)
    }
}