/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.settings

import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import kotlinx.coroutines.flow.Flow

interface SettingsProvider {
    val initialized: Flow<Boolean>

    // UX
    var isDarkTheme: Boolean
    var orderByFirstName: Boolean

    var showInitialAppInfoDialog: Boolean

    // Defaults
    var defaultContactType: ContactType

    // Incoming Call Detection
    var requestIncomingCallPermissions: Boolean
    var showIncomingCallsOnLockScreen: Boolean
    var useBroadcastReceiverForIncomingCalls: Boolean

    // Others
    var sendErrorsToCrashlytics: Boolean
}
