/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.settings

import ch.abwesend.privatecontacts.domain.model.contact.ContactType

interface ISettingsState {
    // UX
    val isDarkTheme: Boolean
    val orderByFirstName: Boolean

    val showInitialAppInfoDialog: Boolean

    // Defaults
    val defaultContactType: ContactType

    // Incoming Call Detection
    val requestIncomingCallPermissions: Boolean
    val showIncomingCallsOnLockScreen: Boolean
    val useBroadcastReceiverForIncomingCalls: Boolean

    // Others
    val sendErrorsToCrashlytics: Boolean
}

data class SettingsState(
    override val isDarkTheme: Boolean,
    override val orderByFirstName: Boolean,

    override val showInitialAppInfoDialog: Boolean,

    override val defaultContactType: ContactType,

    override val requestIncomingCallPermissions: Boolean,
    override val showIncomingCallsOnLockScreen: Boolean,
    override val useBroadcastReceiverForIncomingCalls: Boolean,

    override val sendErrorsToCrashlytics: Boolean
) : ISettingsState {
    companion object {
        val defaultSettings: ISettingsState = SettingsState(
            isDarkTheme = false,
            orderByFirstName = true,
            showIncomingCallsOnLockScreen = true,
            showInitialAppInfoDialog = true,
            requestIncomingCallPermissions = true,
            useBroadcastReceiverForIncomingCalls = true,
            sendErrorsToCrashlytics = true,
            defaultContactType = ContactType.PRIVATE,
        )
    }
}
