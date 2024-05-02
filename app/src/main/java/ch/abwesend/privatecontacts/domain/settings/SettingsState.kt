/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.settings

import ch.abwesend.privatecontacts.BuildConfig
import ch.abwesend.privatecontacts.domain.model.contact.ContactAccount
import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.model.importexport.VCardVersion

interface ISettingsState {
    // UX
    val appTheme: AppTheme
    val orderByFirstName: Boolean
    val showContactTypeInList: Boolean

    /** show extra save and cancel buttons at the bottom of the edit-screen */
    val showExtraButtonsInEditScreen: Boolean

    /** show the top-bar at the bottom */
    val invertTopAndBottomBars: Boolean

    val showInitialAppInfoDialog: Boolean

    /** shows a button to open a phone-number in WhatsApp */
    val showWhatsAppButtons: Boolean

    // Defaults
    val defaultContactType: ContactType
    val defaultExternalContactAccount: ContactAccount
    val defaultVCardVersion: VCardVersion

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

    override val showExtraButtonsInEditScreen: Boolean,
    override val invertTopAndBottomBars: Boolean,

    override val showInitialAppInfoDialog: Boolean,

    override val showWhatsAppButtons: Boolean,

    override val defaultContactType: ContactType,
    override val defaultExternalContactAccount: ContactAccount,
    override val defaultVCardVersion: VCardVersion,

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
            showExtraButtonsInEditScreen = true,
            invertTopAndBottomBars = false,
            showIncomingCallsOnLockScreen = true,
            showInitialAppInfoDialog = true,
            showWhatsAppButtons = true,
            requestIncomingCallPermissions = true,
            observeIncomingCalls = true,
            showAndroidContacts = true,
            sendErrorsToCrashlytics = true,
            defaultContactType = ContactType.default,
            defaultExternalContactAccount = ContactAccount.defaultForExternal,
            defaultVCardVersion = VCardVersion.default,
            currentVersion = BuildConfig.VERSION_CODE,
        )
    }
}
