/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.settings

import ch.abwesend.privatecontacts.domain.model.appearance.SecondTabMode
import ch.abwesend.privatecontacts.domain.model.backup.BackupContactScope
import ch.abwesend.privatecontacts.domain.model.backup.BackupFrequency
import ch.abwesend.privatecontacts.domain.model.backup.NumberOfBackupsToKeep
import ch.abwesend.privatecontacts.domain.model.contact.ContactAccount
import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.model.importexport.VCardVersion
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface SettingsRepository : ISettingsState {
    val settings: Flow<ISettingsState>
    suspend fun nextSettings(): ISettingsState?
    fun overrideSettingsWith(settings: ISettingsState)

    // UX
    override var appTheme: AppTheme
    override var appLanguage: AppLanguage

    override var orderByFirstName: Boolean
    override var showContactTypeInList: Boolean

    override var showExtraButtonsInEditScreen: Boolean
    override var invertTopAndBottomBars: Boolean

    override var showInitialAppInfoDialog: Boolean // invisible

    override var showWhatsAppButtons: Boolean

    override var secondTabMode: SecondTabMode

    // Defaults
    override var defaultContactType: ContactType
    override var defaultExternalContactAccount: ContactAccount
    override var defaultVCardVersion: VCardVersion

    // Incoming Call Detection
    override var observeIncomingCalls: Boolean
    override var requestIncomingCallPermissions: Boolean // invisible
    override var showIncomingCallsOnLockScreen: Boolean
    override var blockIncomingCallsFromUnknownNumbers: Boolean

    // Android Contacts
    override var showAndroidContacts: Boolean
    override var showThirdPartyContactAccounts: Boolean

    // Security
    override var authenticationRequired: Boolean

    // Privacy
    override var useAlternativeAppIcon: Boolean
    override var sendErrorsToCrashlytics: Boolean

    // Periodic Backup
    override var backupFrequency: BackupFrequency
    override var backupContactScope: BackupContactScope
    override var backupFolder: String
    override var lastBackupDate: LocalDate
    override var backupEncryptionEnabled: Boolean
    override var backupPasswordEncrypted: String
    override var numberOfBackupsToKeep: NumberOfBackupsToKeep

    // Feature Flags
    override var useNewContactsLibrary: Boolean

    // Others
    override var currentVersion: Int
    override var previousVersion: Int
    override var numberOfAppStarts: Int
    override var latestUserPromptAtStartup: LocalDate
    override var showReviewDialog: Boolean
}
