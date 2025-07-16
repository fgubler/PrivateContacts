/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.settings

import ch.abwesend.privatecontacts.BuildConfig
import ch.abwesend.privatecontacts.domain.model.appearance.SecondTabMode
import ch.abwesend.privatecontacts.domain.model.contact.ContactAccount
import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.model.importexport.VCardVersion
import java.time.LocalDate

interface ISettingsState {
    // UX
    val appTheme: AppTheme
    val appLanguage: AppLanguage

    val orderByFirstName: Boolean
    val showContactTypeInList: Boolean

    /** show extra save and cancel buttons at the bottom of the edit-screen */
    val showExtraButtonsInEditScreen: Boolean

    /** show the top-bar at the bottom */
    val invertTopAndBottomBars: Boolean

    val showInitialAppInfoDialog: Boolean

    /** shows a button to open a phone-number in WhatsApp */
    val showWhatsAppButtons: Boolean

    /** what kind of contacts the second tab should show */
    val secondTabMode: SecondTabMode

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

    // Security
    val authenticationRequired: Boolean

    // Privacy
    val useAlternativeAppIcon: Boolean
    val sendErrorsToCrashlytics: Boolean

    // Others
    val currentVersion: Int
    val previousVersion: Int
    val numberOfAppStarts: Int
    val latestUserPromptAtStartup: LocalDate
    val showReviewDialog: Boolean
}

data class SettingsState(
    override val appTheme: AppTheme,
    override val appLanguage: AppLanguage,

    override val orderByFirstName: Boolean,
    override val showContactTypeInList: Boolean,

    override val showExtraButtonsInEditScreen: Boolean,
    override val invertTopAndBottomBars: Boolean,

    override val showInitialAppInfoDialog: Boolean,

    override val showWhatsAppButtons: Boolean,
    override val secondTabMode: SecondTabMode,

    override val defaultContactType: ContactType,
    override val defaultExternalContactAccount: ContactAccount,
    override val defaultVCardVersion: VCardVersion,

    override val requestIncomingCallPermissions: Boolean,
    override val observeIncomingCalls: Boolean,
    override val showIncomingCallsOnLockScreen: Boolean,

    override val showAndroidContacts: Boolean,

    override val authenticationRequired: Boolean,

    override val useAlternativeAppIcon: Boolean,
    override val sendErrorsToCrashlytics: Boolean,

    override val currentVersion: Int,
    override val previousVersion: Int,
    override val numberOfAppStarts: Int,
    override val latestUserPromptAtStartup: LocalDate,
    override val showReviewDialog: Boolean,
) : ISettingsState {
    companion object {
        val defaultSettings: ISettingsState = SettingsState(
            appTheme = AppTheme.SYSTEM_SETTINGS,
            appLanguage = AppLanguage.SYSTEM_DEFAULT,
            orderByFirstName = true,
            showContactTypeInList = true,
            showExtraButtonsInEditScreen = true,
            invertTopAndBottomBars = false,
            showIncomingCallsOnLockScreen = true,
            showInitialAppInfoDialog = true,
            showWhatsAppButtons = true,
            secondTabMode = SecondTabMode.ALL_CONTACTS,
            requestIncomingCallPermissions = true,
            observeIncomingCalls = true,
            showAndroidContacts = true,
            authenticationRequired = false,
            useAlternativeAppIcon = false,
            sendErrorsToCrashlytics = true,
            defaultContactType = ContactType.default,
            defaultExternalContactAccount = ContactAccount.defaultForExternal,
            defaultVCardVersion = VCardVersion.default,
            currentVersion = BuildConfig.VERSION_CODE,
            previousVersion = BuildConfig.VERSION_CODE,
            numberOfAppStarts = 0,
            latestUserPromptAtStartup = LocalDate.MIN,
            showReviewDialog = true,
        )
    }
}
