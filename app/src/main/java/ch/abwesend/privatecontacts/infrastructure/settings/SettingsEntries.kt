/*
 * internal Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.settings

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import ch.abwesend.privatecontacts.domain.model.contact.accountProviderOrNull
import ch.abwesend.privatecontacts.domain.model.contact.usernameOrNull
import ch.abwesend.privatecontacts.domain.settings.SettingsState.Companion.defaultSettings
import java.time.LocalDate

internal data class SettingsEntry<T>(val key: Preferences.Key<T>, val defaultValue: T)
internal data class EnumSettingsEntry<T : Enum<T>>(val key: Preferences.Key<String>, val defaultValue: T)
internal data class DateSettingsEntry(val key: Preferences.Key<String>, val defaultValue: LocalDate)

internal val appThemeEntry = EnumSettingsEntry(
    key = stringPreferencesKey("appTheme"),
    defaultValue = defaultSettings.appTheme
)
internal val addHorizontalSafeAreaPaddingEntry = SettingsEntry(
    key = booleanPreferencesKey("addHorizontalSafeAreaPadding"),
    defaultValue = defaultSettings.orderByFirstName
)
internal val orderByFirstNameEntry = SettingsEntry(
    key = booleanPreferencesKey("orderByFirstName"),
    defaultValue = defaultSettings.orderByFirstName
)
internal val showContactTypeInListEntry = SettingsEntry(
    key = booleanPreferencesKey("showContactTypeInList"),
    defaultValue = defaultSettings.showContactTypeInList
)
internal val showExtraButtonsInEditScreenEntry = SettingsEntry(
    key = booleanPreferencesKey("showExtraButtonsInEditScreen"),
    defaultValue = defaultSettings.showExtraButtonsInEditScreen
)
internal val invertTopAndBottomBarsEntry = SettingsEntry(
    key = booleanPreferencesKey("invertTopAndBottomBars"),
    defaultValue = defaultSettings.invertTopAndBottomBars
)
internal val incomingCallsOnLockScreenEntry = SettingsEntry(
    key = booleanPreferencesKey("showIncomingCallsOnLockScreen"),
    defaultValue = defaultSettings.showIncomingCallsOnLockScreen
)
internal val showAndroidContactsEntry = SettingsEntry(
    key = booleanPreferencesKey("showAndroidContacts"),
    defaultValue = defaultSettings.showAndroidContacts
)
internal val authenticationRequiredEntry = SettingsEntry(
    key = booleanPreferencesKey("authenticationRequired"),
    defaultValue = defaultSettings.authenticationRequired
)
internal val initialInfoDialogEntry = SettingsEntry(
    key = booleanPreferencesKey("showInitialInfoDialog"),
    defaultValue = defaultSettings.showInitialAppInfoDialog
)
internal val showWhatsAppButtonsEntry = SettingsEntry(
    key = booleanPreferencesKey("showWhatsAppButtons"),
    defaultValue = defaultSettings.showWhatsAppButtons
)
internal val requestIncomingCallPermissionsEntry = SettingsEntry(
    key = booleanPreferencesKey("requestIncomingCallPermissions"),
    defaultValue = defaultSettings.requestIncomingCallPermissions
)
internal val observeIncomingCallsEntry = SettingsEntry(
    key = booleanPreferencesKey("observeIncomingCalls"),
    defaultValue = defaultSettings.observeIncomingCalls
)
internal val sendErrorsToCrashlyticsEntry = SettingsEntry(
    key = booleanPreferencesKey("sendErrorsToCrashlytics"),
    defaultValue = defaultSettings.sendErrorsToCrashlytics
)
internal val defaultContactTypeEntry = EnumSettingsEntry(
    key = stringPreferencesKey("defaultContactType"),
    defaultValue = defaultSettings.defaultContactType
)
internal val defaultExternalContactAccountTypeEntry = EnumSettingsEntry(
    key = stringPreferencesKey("defaultExternalContactAccountType"),
    defaultValue = defaultSettings.defaultExternalContactAccount.type
)
internal val defaultExternalContactAccountUsernameEntry = SettingsEntry(
    key = stringPreferencesKey("defaultExternalContactAccountUsername"),
    defaultValue = defaultSettings.defaultExternalContactAccount.usernameOrNull.orEmpty()
)
internal val defaultExternalContactAccountProviderEntry = SettingsEntry(
    key = stringPreferencesKey("defaultExternalContactAccountProvider"),
    defaultValue = defaultSettings.defaultExternalContactAccount.accountProviderOrNull.orEmpty()
)
internal val defaultVCardVersionEntry = EnumSettingsEntry(
    key = stringPreferencesKey("defaultVCardVersion"),
    defaultValue = defaultSettings.defaultVCardVersion
)

internal val currentVersionEntry = SettingsEntry(
    key = intPreferencesKey("currentVersion"),
    defaultValue = defaultSettings.currentVersion
)

internal val numberOfAppStartsEntry = SettingsEntry(
    key = intPreferencesKey("numberOfAppStarts"),
    defaultValue = defaultSettings.numberOfAppStarts
)

internal val latestUserPromptAtStartupEntry = DateSettingsEntry(
    key = stringPreferencesKey("latestUserPromptAtStartup"),
    defaultValue = defaultSettings.latestUserPromptAtStartup
)

internal val showReviewDialogEntry = SettingsEntry(
    key = booleanPreferencesKey("showReviewDialog"),
    defaultValue = defaultSettings.showReviewDialog
)

// ==========================================
// When adding a settings entry, don't forget to extend the method `overrideSettingsWith()`
// in DataStoreSettingsRepository.
// ==========================================
