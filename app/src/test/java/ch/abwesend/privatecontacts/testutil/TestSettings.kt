/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.testutil

import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.settings.ISettingsState
import ch.abwesend.privatecontacts.domain.settings.SettingsRepository
import ch.abwesend.privatecontacts.domain.settings.SettingsState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

data class TestSettings(
    private val settings: ISettingsState = SettingsState.defaultSettings,
    override val initialized: Flow<Boolean> = flow { emit(true) },
    override val currentSettings: Flow<ISettingsState> = flow { emit(settings) },
) : SettingsRepository {
    override var isDarkTheme: Boolean = settings.isDarkTheme
    override var orderByFirstName: Boolean = settings.orderByFirstName
    override var showInitialAppInfoDialog: Boolean = settings.showInitialAppInfoDialog
    override var defaultContactType: ContactType = settings.defaultContactType
    override var requestIncomingCallPermissions: Boolean = settings.requestIncomingCallPermissions
    override var showIncomingCallsOnLockScreen: Boolean = settings.showIncomingCallsOnLockScreen
    override var useBroadcastReceiverForIncomingCalls: Boolean = settings.useBroadcastReceiverForIncomingCalls
    override var sendErrorsToCrashlytics: Boolean = settings.sendErrorsToCrashlytics
}
