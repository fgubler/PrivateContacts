/*
 * internal Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.contact.ContactAccount
import ch.abwesend.privatecontacts.domain.model.contact.ContactAccountType
import ch.abwesend.privatecontacts.domain.settings.ISettingsState
import ch.abwesend.privatecontacts.domain.settings.SettingsState
import ch.abwesend.privatecontacts.domain.util.applicationScope
import kotlinx.coroutines.launch

internal fun Preferences.createSettingsState(): ISettingsState = SettingsState(
    appTheme = tryGetEnumValue(darkThemeEntry),
    orderByFirstName = getValue(orderByFirstNameEntry),
    showContactTypeInList = getValue(showContactTypeInListEntry),
    showExtraButtonsInEditScreen = getValue(showExtraButtonsInEditScreenEntry),
    invertTopAndBottomBars = getValue(invertTopAndBottomBarsEntry),
    showIncomingCallsOnLockScreen = getValue(incomingCallsOnLockScreenEntry),
    showInitialAppInfoDialog = getValue(initialInfoDialogEntry),
    requestIncomingCallPermissions = getValue(requestIncomingCallPermissionsEntry),
    observeIncomingCalls = getValue(observeIncomingCallsEntry),
    showAndroidContacts = getValue(showAndroidContactsEntry),
    sendErrorsToCrashlytics = getValue(sendErrorsToCrashlyticsEntry),
    defaultContactType = tryGetEnumValue(defaultContactTypeEntry),
    defaultExternalContactAccount = buildDefaultContactAccount(),
    defaultVCardVersion = tryGetEnumValue(defaultVCardVersionEntry),
    currentVersion = getValue(currentVersionEntry),
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

internal inline fun <reified T : Enum<T>> Preferences.tryGetEnumValue(
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

private fun Preferences.buildDefaultContactAccount(): ContactAccount {
    val type = tryGetEnumValue(defaultExternalContactAccountTypeEntry)
    logger.debug("loaded default contact account type $type")

    return when (type) {
        ContactAccountType.NONE -> ContactAccount.None
        ContactAccountType.LOCAL_PHONE_CONTACTS -> ContactAccount.LocalPhoneContacts
        ContactAccountType.ONLINE_ACCOUNT -> {
            val username = getValue(defaultExternalContactAccountUsernameEntry)
            val provider = getValue(defaultExternalContactAccountProviderEntry)

            if (username.isNotEmpty() && provider.isNotEmpty()) {
                ContactAccount.OnlineAccount(username = username, accountProvider = provider)
            } else ContactAccount.defaultForExternal
        }
    }
}
