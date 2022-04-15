/*
 * internal Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.settings

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import ch.abwesend.privatecontacts.domain.settings.ISettingsState
import ch.abwesend.privatecontacts.domain.settings.SettingsState

internal data class SettingsEntry<T>(val key: Preferences.Key<T>, val defaultValue: T)
internal data class EnumSettingsEntry<T : Enum<T>>(val key: Preferences.Key<String>, val defaultValue: T)

internal val darkThemeEntry = SettingsEntry(
    key = booleanPreferencesKey("isDarkTheme"),
    defaultValue = SettingsState.defaultSettings.isDarkTheme
)
internal val orderByFirstNameEntry = SettingsEntry(
    key = booleanPreferencesKey("orderByFirstName"),
    defaultValue = SettingsState.defaultSettings.orderByFirstName
)
internal val incomingCallsOnLockScreenEntry = SettingsEntry(
    key = booleanPreferencesKey("showIncomingCallsOnLockScreen"),
    defaultValue = SettingsState.defaultSettings.showIncomingCallsOnLockScreen
)
internal val initialInfoDialogEntry = SettingsEntry(
    key = booleanPreferencesKey("showInitialInfoDialog"),
    defaultValue = SettingsState.defaultSettings.showInitialAppInfoDialog
)
internal val requestIncomingCallPermissionsEntry = SettingsEntry(
    key = booleanPreferencesKey("requestIncomingCallPermissions"),
    defaultValue = SettingsState.defaultSettings.requestIncomingCallPermissions
)
internal val useIncomingCallBroadCastReceiverEntry = SettingsEntry(
    key = booleanPreferencesKey("useIncomingCallBroadCastReceiver"),
    defaultValue = SettingsState.defaultSettings.useBroadcastReceiverForIncomingCalls
)
internal val sendErrorsToCrashlyticsEntry = SettingsEntry(
    key = booleanPreferencesKey("sendErrorsToCrashlytics"),
    defaultValue = SettingsState.defaultSettings.sendErrorsToCrashlytics
)
internal val defaultContactTypeEntry = EnumSettingsEntry(
    key = stringPreferencesKey("defaultContactType"),
    defaultValue = SettingsState.defaultSettings.defaultContactType
)

internal val defaultSettingsState: ISettingsState = SettingsState(
    isDarkTheme = darkThemeEntry.defaultValue,
    orderByFirstName = orderByFirstNameEntry.defaultValue,
    showIncomingCallsOnLockScreen = incomingCallsOnLockScreenEntry.defaultValue,
    showInitialAppInfoDialog = initialInfoDialogEntry.defaultValue,
    requestIncomingCallPermissions = requestIncomingCallPermissionsEntry.defaultValue,
    useBroadcastReceiverForIncomingCalls = useIncomingCallBroadCastReceiverEntry.defaultValue,
    sendErrorsToCrashlytics = sendErrorsToCrashlyticsEntry.defaultValue,
    defaultContactType = defaultContactTypeEntry.defaultValue,
)
