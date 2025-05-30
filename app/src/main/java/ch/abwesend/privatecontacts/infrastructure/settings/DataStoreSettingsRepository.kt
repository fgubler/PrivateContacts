/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import ch.abwesend.privatecontacts.domain.lib.coroutine.IDispatchers
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.contact.ContactAccount
import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.model.contact.accountProviderOrNull
import ch.abwesend.privatecontacts.domain.model.contact.usernameOrNull
import ch.abwesend.privatecontacts.domain.model.importexport.VCardVersion
import ch.abwesend.privatecontacts.domain.settings.AppTheme
import ch.abwesend.privatecontacts.domain.settings.ISettingsState
import ch.abwesend.privatecontacts.domain.settings.SettingsRepository
import ch.abwesend.privatecontacts.domain.settings.SettingsState
import ch.abwesend.privatecontacts.domain.util.applicationScope
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.LocalDate

class DataStoreSettingsRepository(context: Context) : SettingsRepository {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
    private val dataStore = context.dataStore

    private val dispatchers: IDispatchers by injectAnywhere()
    private val coroutineScope = applicationScope

    private var currentSettings: ISettingsState = SettingsState.defaultSettings

    override val settings: Flow<ISettingsState> = dataStore.data.map { it.createSettingsState() }

    init {
        coroutineScope.launch(dispatchers.io) {
            settings.collectLatest { settings ->
                currentSettings = settings
                logger.debug("New settings: $currentSettings")
            }
        }
    }

    override var appTheme: AppTheme
        get() = currentSettings.appTheme
        set(value) = dataStore.setEnumValue(appThemeEntry, value)

    override var orderByFirstName: Boolean
        get() = currentSettings.orderByFirstName
        set(value) = dataStore.setValue(orderByFirstNameEntry, value)

    override var showContactTypeInList: Boolean
        get() = currentSettings.showContactTypeInList
        set(value) = dataStore.setValue(showContactTypeInListEntry, value)

    override var showExtraButtonsInEditScreen: Boolean
        get() = currentSettings.showExtraButtonsInEditScreen
        set(value) { dataStore.setValue(showExtraButtonsInEditScreenEntry, value) }

    override var invertTopAndBottomBars: Boolean
        get() = currentSettings.invertTopAndBottomBars
        set(value) { dataStore.setValue(invertTopAndBottomBarsEntry, value) }

    override var showIncomingCallsOnLockScreen: Boolean
        get() = currentSettings.showIncomingCallsOnLockScreen
        set(value) = dataStore.setValue(incomingCallsOnLockScreenEntry, value)

    override var showAndroidContacts: Boolean
        get() = currentSettings.showAndroidContacts
        set(value) = dataStore.setValue(showAndroidContactsEntry, value)

    override var authenticationRequired: Boolean
        get() = currentSettings.authenticationRequired
        set(value) = dataStore.setValue(authenticationRequiredEntry, value)

    override var showInitialAppInfoDialog: Boolean
        get() = currentSettings.showInitialAppInfoDialog
        set(value) = dataStore.setValue(initialInfoDialogEntry, value)

    override var showWhatsAppButtons: Boolean
        get() = currentSettings.showWhatsAppButtons
        set(value) = dataStore.setValue(showWhatsAppButtonsEntry, value)

    override var requestIncomingCallPermissions: Boolean
        get() = currentSettings.requestIncomingCallPermissions
        set(value) = dataStore.setValue(requestIncomingCallPermissionsEntry, value)

    override var observeIncomingCalls: Boolean
        get() = currentSettings.observeIncomingCalls
        set(value) = dataStore.setValue(observeIncomingCallsEntry, value)

    override var useAlternativeAppIcon: Boolean
        get() = currentSettings.useAlternativeAppIcon
        set(value) = dataStore.setValue(useAlternativeAppIconEntry, value)

    override var sendErrorsToCrashlytics: Boolean
        get() = currentSettings.sendErrorsToCrashlytics
        set(value) = dataStore.setValue(sendErrorsToCrashlyticsEntry, value)

    override var currentVersion: Int
        get() = currentSettings.currentVersion
        set(value) = dataStore.setValue(currentVersionEntry, value)
    override var numberOfAppStarts: Int
        get() = currentSettings.numberOfAppStarts
        set(value) = dataStore.setValue(numberOfAppStartsEntry, value)
    override var latestUserPromptAtStartup: LocalDate
        get() = currentSettings.latestUserPromptAtStartup
        set(value) = dataStore.setDateValue(latestUserPromptAtStartupEntry, value)
    override var showReviewDialog: Boolean
        get() = currentSettings.showReviewDialog
        set(value) = dataStore.setValue(showReviewDialogEntry, value)

    override var defaultContactType: ContactType
        get() = currentSettings.defaultContactType
        set(value) = dataStore.setEnumValue(defaultContactTypeEntry, value)

    override var defaultExternalContactAccount: ContactAccount
        get() = currentSettings.defaultExternalContactAccount
        set(value) {
            dataStore.setEnumValue(defaultExternalContactAccountTypeEntry, value.type)
            dataStore.setValue(defaultExternalContactAccountUsernameEntry, value.usernameOrNull.orEmpty())
            dataStore.setValue(defaultExternalContactAccountProviderEntry, value.accountProviderOrNull.orEmpty())
        }

    override var defaultVCardVersion: VCardVersion
        get() = currentSettings.defaultVCardVersion
        set(value) = dataStore.setEnumValue(defaultVCardVersionEntry, value)

    override fun overrideSettingsWith(settings: ISettingsState) {
        appTheme = settings.appTheme
        orderByFirstName = settings.orderByFirstName
        showContactTypeInList = settings.showContactTypeInList
        showExtraButtonsInEditScreen = settings.showExtraButtonsInEditScreen
        invertTopAndBottomBars = settings.invertTopAndBottomBars
        showIncomingCallsOnLockScreen = settings.showIncomingCallsOnLockScreen
        showInitialAppInfoDialog = settings.showInitialAppInfoDialog
        showWhatsAppButtons = settings.showWhatsAppButtons
        requestIncomingCallPermissions = settings.requestIncomingCallPermissions
        observeIncomingCalls = settings.observeIncomingCalls
        showAndroidContacts = settings.showAndroidContacts
        useAlternativeAppIcon = settings.useAlternativeAppIcon
        sendErrorsToCrashlytics = settings.sendErrorsToCrashlytics
        defaultContactType = settings.defaultContactType
        defaultExternalContactAccount = settings.defaultExternalContactAccount
        showReviewDialog = settings.showReviewDialog

        /*
           meta-data is not changed
               - currentVersion
               - numberOfAppStarts
               - latestUserPromptAtStartup
         */

        // TODO add new properties here
    }
}
