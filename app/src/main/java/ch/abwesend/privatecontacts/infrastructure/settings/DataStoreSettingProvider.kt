/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import ch.abwesend.privatecontacts.domain.lib.coroutine.IDispatchers
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.settings.SettingsProvider
import ch.abwesend.privatecontacts.domain.util.applicationScope
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch

class DataStoreSettingProvider(context: Context) : SettingsProvider {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
    private val dataStore = context.dataStore

    private val dispatchers: IDispatchers by injectAnywhere()
    private val coroutineScope = applicationScope

    private var currentData: Preferences? = null

    override val initialized: Flow<Boolean> = dataStore.data
        .map { true }
        .onStart { emit(false) }
        .take(2)

    init {
        coroutineScope.launch(dispatchers.io) {
            context.dataStore.data.collectLatest { preferences ->
                currentData = preferences
                logger.debug("New settings: $preferences")
            }
        }
    }

    override var isDarkTheme: Boolean
        get() = with(darkThemeEntry) { currentData?.get(key) ?: defaultValue }
        set(value) = dataStore.setValue(darkThemeEntry, value)

    override var orderByFirstName: Boolean
        get() = with(orderByFirstNameEntry) { currentData?.get(key) ?: defaultValue }
        set(value) = dataStore.setValue(orderByFirstNameEntry, value)

    override var showIncomingCallsOnLockScreen: Boolean
        get() = with(incomingCallsOnLockScreenEntry) { currentData?.get(key) ?: defaultValue }
        set(value) = dataStore.setValue(incomingCallsOnLockScreenEntry, value)

    override var showInitialAppInfoDialog: Boolean
        get() = with(initialInfoDialogEntry) { currentData?.get(key) ?: defaultValue }
        set(value) = dataStore.setValue(initialInfoDialogEntry, value)

    override var requestIncomingCallPermissions: Boolean
        get() = with(requestIncomingCallPermissionsEntry) { currentData?.get(key) ?: defaultValue }
        set(value) = dataStore.setValue(requestIncomingCallPermissionsEntry, value)

    override var useBroadcastReceiverForIncomingCalls: Boolean
        get() = with(useIncomingCallBroadCastReceiverEntry) { currentData?.get(key) ?: defaultValue }
        set(value) = dataStore.setValue(useIncomingCallBroadCastReceiverEntry, value)

    override var defaultContactType: ContactType
        get() = currentData.tryGetEnum(defaultContactTypeEntry)
        set(value) = dataStore.setEnumValue(defaultContactTypeEntry, value)
}

private fun <T> DataStore<Preferences>.setValue(settingsEntry: SettingsEntry<T>, value: T) {
    applicationScope.launch {
        edit { preferences ->
            preferences[settingsEntry.key] = value
        }
    }
}

private inline fun <reified T : Enum<T>> Preferences?.tryGetEnum(
    settingsEntry: EnumSettingsEntry<T>
): T {
    val rawValue = this?.get(settingsEntry.key)
    val parsedValue = try {
        rawValue?.let { enumValueOf<T>(rawValue) }
    } catch (e: IllegalArgumentException) {
        null
    }
    return parsedValue ?: settingsEntry.defaultValue
}

private fun <T : Enum<T>> DataStore<Preferences>.setEnumValue(settingsEntry: EnumSettingsEntry<T>, value: T) {
    applicationScope.launch {
        edit { preferences ->
            preferences[settingsEntry.key] = value.name
        }
    }
}
