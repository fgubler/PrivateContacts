/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.testutil

import ch.abwesend.privatecontacts.domain.model.appearance.SecondTabMode
import ch.abwesend.privatecontacts.domain.model.contact.ContactAccount
import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.model.importexport.VCardVersion
import ch.abwesend.privatecontacts.domain.settings.AppTheme
import ch.abwesend.privatecontacts.domain.settings.ISettingsState
import ch.abwesend.privatecontacts.domain.settings.SettingsRepository
import ch.abwesend.privatecontacts.domain.settings.SettingsState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.LocalDate

data class TestSettings(
    private val currentSettings: ISettingsState = SettingsState.defaultSettings,
    override val settings: Flow<ISettingsState> = flow { emit(currentSettings) },
) : SettingsRepository {
    override var appTheme: AppTheme = currentSettings.appTheme
    override var orderByFirstName: Boolean = currentSettings.orderByFirstName
    override var showContactTypeInList: Boolean = currentSettings.showContactTypeInList
    override var showInitialAppInfoDialog: Boolean = currentSettings.showInitialAppInfoDialog
    override var showWhatsAppButtons: Boolean = currentSettings.showWhatsAppButtons
    override var secondTabMode: SecondTabMode = currentSettings.secondTabMode
    override var defaultContactType: ContactType = currentSettings.defaultContactType
    override var observeIncomingCalls: Boolean = currentSettings.observeIncomingCalls
    override var requestIncomingCallPermissions: Boolean = currentSettings.requestIncomingCallPermissions
    override var showIncomingCallsOnLockScreen: Boolean = currentSettings.showIncomingCallsOnLockScreen
    override var showAndroidContacts: Boolean = currentSettings.showAndroidContacts
    override var authenticationRequired: Boolean = currentSettings.authenticationRequired
    override var useAlternativeAppIcon: Boolean = currentSettings.useAlternativeAppIcon
    override var sendErrorsToCrashlytics: Boolean = currentSettings.sendErrorsToCrashlytics
    override var currentVersion: Int = currentSettings.currentVersion
    override var previousVersion: Int = currentSettings.previousVersion
    override var numberOfAppStarts: Int = currentSettings.numberOfAppStarts
    override var latestUserPromptAtStartup: LocalDate = currentSettings.latestUserPromptAtStartup
    override var showReviewDialog: Boolean = currentSettings.showReviewDialog
    override var defaultExternalContactAccount: ContactAccount = currentSettings.defaultExternalContactAccount
    override var defaultVCardVersion: VCardVersion = currentSettings.defaultVCardVersion
    override var showExtraButtonsInEditScreen: Boolean = currentSettings.showExtraButtonsInEditScreen
    override var invertTopAndBottomBars: Boolean = currentSettings.invertTopAndBottomBars

    override suspend fun nextSettings(): ISettingsState {
        return currentSettings
    }

    override fun overrideSettingsWith(settings: ISettingsState) {
        // Do nothing
    }
}
