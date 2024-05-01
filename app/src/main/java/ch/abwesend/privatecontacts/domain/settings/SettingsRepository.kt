/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.settings

import ch.abwesend.privatecontacts.domain.model.contact.ContactAccount
import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.model.importexport.VCardVersion
import kotlinx.coroutines.flow.Flow

interface SettingsRepository : ISettingsState {
    val settings: Flow<ISettingsState>
    fun overrideSettingsWith(settings: ISettingsState)

    // UX
    override var appTheme: AppTheme
    override var orderByFirstName: Boolean
    override var showContactTypeInList: Boolean

    override var showExtraButtonsInEditScreen: Boolean
    override var invertTopAndBottomBars: Boolean

    override var showInitialAppInfoDialog: Boolean // invisible

    override var showWhatsAppButtons: Boolean

    // Defaults
    override var defaultContactType: ContactType
    override var defaultExternalContactAccount: ContactAccount
    override var defaultVCardVersion: VCardVersion

    // Incoming Call Detection
    override var observeIncomingCalls: Boolean
    override var requestIncomingCallPermissions: Boolean // invisible
    override var showIncomingCallsOnLockScreen: Boolean

    // Android Contacts
    override var showAndroidContacts: Boolean

    // Others
    override var sendErrorsToCrashlytics: Boolean
    override var currentVersion: Int
}
