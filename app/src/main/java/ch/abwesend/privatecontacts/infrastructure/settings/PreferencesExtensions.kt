/*
 * internal Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import ch.abwesend.privatecontacts.domain.settings.ISettingsState
import ch.abwesend.privatecontacts.domain.settings.SettingsState
import ch.abwesend.privatecontacts.domain.util.applicationScope
import kotlinx.coroutines.launch

internal fun Preferences.createSettingsState(): ISettingsState = SettingsState(
    isDarkTheme = getValue(darkThemeEntry),
    orderByFirstName = getValue(orderByFirstNameEntry),
    showIncomingCallsOnLockScreen = getValue(incomingCallsOnLockScreenEntry),
    showInitialAppInfoDialog = getValue(initialInfoDialogEntry),
    requestIncomingCallPermissions = getValue(requestIncomingCallPermissionsEntry),
    useBroadcastReceiverForIncomingCalls = getValue(useIncomingCallBroadCastReceiverEntry),
    sendErrorsToCrashlytics = getValue(sendErrorsToCrashlyticsEntry),
    defaultContactType = tryGetEnum(defaultContactTypeEntry),
)

internal fun <T> Preferences.getValue(settingsEntry: SettingsEntry<T>): T =
    get(settingsEntry.key) ?: settingsEntry.defaultValue

internal fun <T> DataStore<Preferences>.setValue(settingsEntry: SettingsEntry<T>, value: T) {
    applicationScope.launch {
        edit { preferences ->
            preferences[settingsEntry.key] = value
        }
    }
}

internal inline fun <reified T : Enum<T>> Preferences.tryGetEnum(
    settingsEntry: EnumSettingsEntry<T>
): T {
    val rawValue = this[settingsEntry.key]
    val parsedValue = try {
        rawValue?.let { enumValueOf<T>(rawValue) }
    } catch (e: IllegalArgumentException) {
        null
    }
    return parsedValue ?: settingsEntry.defaultValue
}

internal fun <T : Enum<T>> DataStore<Preferences>.setEnumValue(settingsEntry: EnumSettingsEntry<T>, value: T) {
    applicationScope.launch {
        edit { preferences ->
            preferences[settingsEntry.key] = value.name
        }
    }
}
