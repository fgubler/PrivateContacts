/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import ch.abwesend.privatecontacts.domain.lib.coroutine.IDispatchers
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.settings.AppTheme
import ch.abwesend.privatecontacts.domain.settings.ISettingsState
import ch.abwesend.privatecontacts.domain.settings.SettingsRepository
import ch.abwesend.privatecontacts.domain.util.applicationScope
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch

class DataStoreSettingsRepository(context: Context) : SettingsRepository {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
    private val dataStore = context.dataStore

    private val dispatchers: IDispatchers by injectAnywhere()
    private val coroutineScope = applicationScope

    private var currentState: ISettingsState = defaultSettingsState

    override val initialized: Flow<Boolean> = dataStore.data
        .map { true }
        .onStart { emit(false) }
        .take(2)

    override val currentSettings: Flow<ISettingsState> = dataStore.data.map { it.createSettingsState() }

    init {
        coroutineScope.launch(dispatchers.io) {
            currentSettings.collectLatest { settings ->
                currentState = settings
                logger.debug("New settings: $currentState")
            }
        }
    }

    override var appTheme: AppTheme
        get() = currentState.appTheme
        set(value) = dataStore.setEnumValue(darkThemeEntry, value)

    override var orderByFirstName: Boolean
        get() = currentState.orderByFirstName
        set(value) = dataStore.setValue(orderByFirstNameEntry, value)

    override var showIncomingCallsOnLockScreen: Boolean
        get() = currentState.showIncomingCallsOnLockScreen
        set(value) = dataStore.setValue(incomingCallsOnLockScreenEntry, value)

    override var showInitialAppInfoDialog: Boolean
        get() = currentState.showInitialAppInfoDialog
        set(value) = dataStore.setValue(initialInfoDialogEntry, value)

    override var requestIncomingCallPermissions: Boolean
        get() = currentState.requestIncomingCallPermissions
        set(value) = dataStore.setValue(requestIncomingCallPermissionsEntry, value)

    override var useBroadcastReceiverForIncomingCalls: Boolean
        get() = currentState.useBroadcastReceiverForIncomingCalls
        set(value) = dataStore.setValue(useIncomingCallBroadCastReceiverEntry, value)

    override var sendErrorsToCrashlytics: Boolean
        get() = currentState.sendErrorsToCrashlytics
        set(value) = dataStore.setValue(sendErrorsToCrashlyticsEntry, value)

    override var defaultContactType: ContactType
        get() = currentState.defaultContactType
        set(value) = dataStore.setEnumValue(defaultContactTypeEntry, value)
}
