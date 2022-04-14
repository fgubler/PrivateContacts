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
    val isDarkTheme: Boolean
    val orderByFirstName: Boolean

    var showInitialAppInfoDialog: Boolean

    // Defaults
    val defaultContactType: ContactType

    // Incoming Call Detection
    var requestIncomingCallPermissions: Boolean
    val showIncomingCallsOnLockScreen: Boolean
    val useBroadcastReceiverForIncomingCalls: Boolean
}
