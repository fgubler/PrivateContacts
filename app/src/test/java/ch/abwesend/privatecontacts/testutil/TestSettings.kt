/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.testutil

import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.settings.AppTheme
import ch.abwesend.privatecontacts.domain.settings.ISettingsState
import ch.abwesend.privatecontacts.domain.settings.SettingsRepository
import ch.abwesend.privatecontacts.domain.settings.SettingsState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

data class TestSettings(
    override val currentSettings: ISettingsState = SettingsState.defaultSettings,
    override val settings: Flow<ISettingsState> = flow { emit(currentSettings) },
) : SettingsRepository {
    override var appTheme: AppTheme = currentSettings.appTheme
    override var orderByFirstName: Boolean = currentSettings.orderByFirstName
    override var showInitialAppInfoDialog: Boolean = currentSettings.showInitialAppInfoDialog
    override var defaultContactType: ContactType = currentSettings.defaultContactType
    override var observeIncomingCalls: Boolean = currentSettings.observeIncomingCalls
    override var requestIncomingCallPermissions: Boolean = currentSettings.requestIncomingCallPermissions
    override var showIncomingCallsOnLockScreen: Boolean = currentSettings.showIncomingCallsOnLockScreen
    override var sendErrorsToCrashlytics: Boolean = currentSettings.sendErrorsToCrashlytics

    override fun overrideSettingsWith(settings: ISettingsState) {
        // Do nothing
    }
}
