/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.settings

import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import kotlinx.coroutines.flow.Flow

interface SettingsRepository : ISettingsState {
    val settings: Flow<ISettingsState>
    val currentSettings: ISettingsState
    fun overrideSettingsWith(settings: ISettingsState)

    // UX
    override var appTheme: AppTheme
    override var orderByFirstName: Boolean

    override var showInitialAppInfoDialog: Boolean // invisible

    // Defaults
    override var defaultContactType: ContactType

    // Incoming Call Detection
    override var requestIncomingCallPermissions: Boolean // invisible
    override var showIncomingCallsOnLockScreen: Boolean
    override var useBroadcastReceiverForIncomingCalls: Boolean // invisible for now (until CallScreeningService...)

    // Others
    override var sendErrorsToCrashlytics: Boolean
}
