/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.settings

import ch.abwesend.privatecontacts.BuildConfig
import ch.abwesend.privatecontacts.domain.model.contact.ContactType

interface ISettingsState {
    // UX
    val appTheme: AppTheme
    val orderByFirstName: Boolean
    val showContactTypeInList: Boolean

    val showInitialAppInfoDialog: Boolean

    // Defaults
    val defaultContactType: ContactType

    // Incoming Call Detection
    /** Whether call-detection should be attempted */
    val observeIncomingCalls: Boolean
    /**
     * Whether the user should be asked for the necessary permissions on startup.
     * If this is true and [observeIncomingCalls] is false, the user will still be asked.
     */
    val requestIncomingCallPermissions: Boolean
    /**
     * Whether to show the notification for incoming calls on the lock-screen.
     * Depends on [observeIncomingCalls]
     */
    val showIncomingCallsOnLockScreen: Boolean

    // Android Contacts
    val showAndroidContacts: Boolean

    // Others
    val sendErrorsToCrashlytics: Boolean
    val currentVersion: Int
}

data class SettingsState(
    override val appTheme: AppTheme,
    override val orderByFirstName: Boolean,
    override val showContactTypeInList: Boolean,

    override val showInitialAppInfoDialog: Boolean,

    override val defaultContactType: ContactType,

    override val requestIncomingCallPermissions: Boolean,
    override val observeIncomingCalls: Boolean,
    override val showIncomingCallsOnLockScreen: Boolean,

    override val showAndroidContacts: Boolean,

    override val sendErrorsToCrashlytics: Boolean,
    override val currentVersion: Int,
) : ISettingsState {
    companion object {
        val defaultSettings: ISettingsState = SettingsState(
            appTheme = AppTheme.SYSTEM_SETTINGS,
            orderByFirstName = true,
            showContactTypeInList = true,
            showIncomingCallsOnLockScreen = true,
            showInitialAppInfoDialog = true,
            requestIncomingCallPermissions = true,
            observeIncomingCalls = true,
            showAndroidContacts = true,
            sendErrorsToCrashlytics = true,
            defaultContactType = ContactType.SECRET,
            currentVersion = BuildConfig.VERSION_CODE,
        )
    }
}
