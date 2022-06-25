/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.settings.AppTheme
import ch.abwesend.privatecontacts.domain.settings.ISettingsState
import ch.abwesend.privatecontacts.domain.settings.Settings
import ch.abwesend.privatecontacts.domain.settings.SettingsRepository
import ch.abwesend.privatecontacts.domain.util.callIdentificationPossible
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import ch.abwesend.privatecontacts.view.initialization.CallPermissionHandler
import ch.abwesend.privatecontacts.view.model.ResDropDownOption
import ch.abwesend.privatecontacts.view.model.ScreenContext
import ch.abwesend.privatecontacts.view.permission.AndroidContactPermissionHelper
import ch.abwesend.privatecontacts.view.permission.CallPermissionHelper
import ch.abwesend.privatecontacts.view.permission.CallScreeningRoleHelper
import ch.abwesend.privatecontacts.view.screens.BaseScreen
import ch.abwesend.privatecontacts.view.util.getCurrentActivity
import ch.abwesend.privatecontacts.view.routing.Screen.Settings as SettingsScreen

@ExperimentalMaterialApi
object SettingsScreen {
    private val callPermissionHelper: CallPermissionHelper by injectAnywhere()
    private val contactPermissionHelper: AndroidContactPermissionHelper by injectAnywhere()
    private val callScreeningRoleHelper: CallScreeningRoleHelper by injectAnywhere()

    var isScrolling: Boolean by mutableStateOf(false) // TODO remove once google issue 212091796 is fixed
        private set

    @Composable
    fun Screen(screenContext: ScreenContext) {
        val settingsRepository = Settings.repository
        val currentSettings = screenContext.settings

        val scrollState = rememberScrollState()
        isScrolling = scrollState.isScrollInProgress

        val callDetectionPossible = remember { callIdentificationPossible }

        BaseScreen(
            screenContext = screenContext,
            selectedScreen = SettingsScreen,
            topBarActions = { SettingsActions(screenContext.settingsViewModel) }
        ) {
            Column(
                modifier = Modifier
                    .padding(10.dp)
                    .verticalScroll(scrollState)
            ) {
                UxCategory(settingsRepository, currentSettings)
                SettingsCategorySpacer()

                if (callDetectionPossible) CallDetectionCategory(settingsRepository, currentSettings)
                else CallDetectionCategoryDummy()
                SettingsCategorySpacer()

                AndroidContactsCategory(settingsRepository, currentSettings)
                SettingsCategorySpacer()

                if (false) { // TODO re-insert when public contacts are possible
                    DefaultValuesCategory(settingsRepository, currentSettings)
                    SettingsCategorySpacer()
                }

                MiscellaneousCategory(settingsRepository, currentSettings)
                SettingsCategorySpacer() // makes sure the last card is not cut off
            }
        }
    }

    @Composable
    private fun UxCategory(settingsRepository: SettingsRepository, currentSettings: ISettingsState) {
        val appThemeOptions = remember {
            AppTheme.values().map { ResDropDownOption(labelRes = it.labelRes, value = it) }
        }

        SettingsCategory(titleRes = R.string.settings_category_ux) {
            SettingsDropDown(
                label = R.string.settings_entry_app_theme,
                description = null,
                value = currentSettings.appTheme,
                options = appThemeOptions,
                onValueChanged = { settingsRepository.appTheme = it }
            )

            SettingsEntryDivider()

            SettingsCheckbox(
                label = R.string.settings_entry_order_by_first_name,
                description = null,
                value = currentSettings.orderByFirstName,
                onValueChanged = { settingsRepository.orderByFirstName = it }
            )
            SettingsCheckbox(
                label = R.string.settings_entry_show_contact_types_on_contact_list,
                description = null,
                value = currentSettings.showContactTypeInList,
                onValueChanged = { settingsRepository.showContactTypeInList = it }
            )
        }
    }

    @Composable
    private fun CallDetectionCategory(settingsRepository: SettingsRepository, currentSettings: ISettingsState) {
        var requestPermissions: Boolean by remember { mutableStateOf(false) }

        SettingsCategory(titleRes = R.string.settings_category_call_detection, infoPopupText = R.string.settings_info_dialog_call_detection) {
            SettingsCheckbox(
                label = R.string.settings_entry_call_detection,
                description = R.string.settings_entry_call_detection_description,
                value = currentSettings.observeIncomingCalls,
            ) {
                settingsRepository.observeIncomingCalls = it
                settingsRepository.requestIncomingCallPermissions = true
                if (it) { requestPermissions = true }
            }

            SettingsEntryDivider()

            SettingsCheckbox(
                label = R.string.settings_entry_show_calls_on_lockscreen,
                description = R.string.settings_entry_show_calls_on_lockscreen_description,
                value = currentSettings.observeIncomingCalls && currentSettings.showIncomingCallsOnLockScreen,
                enabled = currentSettings.observeIncomingCalls
            ) { settingsRepository.showIncomingCallsOnLockScreen = it }
        }

        if (requestPermissions) {
            getCurrentActivity()?.CallPermissionHandler(
                settings = currentSettings,
                permissionHelper = callPermissionHelper,
                roleHelper = callScreeningRoleHelper,
            ) {
                requestPermissions = false
            } ?: logger.warning("Activity not found: cannot ask for permissions")
        }
    }

    @Composable
    private fun CallDetectionCategoryDummy() {
        SettingsCategory(titleRes = R.string.settings_category_call_detection) {
            Text(text = stringResource(id = R.string.settings_category_call_detection_excuse))
        }
    }

    @Composable
    private fun AndroidContactsCategory(settingsRepository: SettingsRepository, currentSettings: ISettingsState) {
        var requestPermissions: Boolean by remember { mutableStateOf(false) }

        SettingsCategory(
            titleRes = R.string.settings_category_contacts,
            infoPopupText = R.string.settings_info_dialog_android_contacts,
            hideInfoPopup = currentSettings.showAndroidContacts,
        ) {
            SettingsCheckbox(
                label = R.string.settings_entry_show_android_contacts,
                description = R.string.settings_entry_show_android_contacts_description,
                value = currentSettings.showAndroidContacts,
            ) {
                settingsRepository.showAndroidContacts = it
                if (it) { requestPermissions = true }
            }
        }

        if (requestPermissions) {
            contactPermissionHelper.requestAndroidContactPermissions { requestPermissions = false }
        }
    }

    @Composable
    private fun DefaultValuesCategory(settingsRepository: SettingsRepository, currentSettings: ISettingsState) {
        val contactTypeOptions = remember {
            ContactType.values().map { ResDropDownOption(labelRes = it.label, value = it) }
        }
        SettingsCategory(titleRes = R.string.settings_category_default_values) {
            SettingsDropDown(
                label = R.string.settings_entry_default_contact_type,
                description = R.string.settings_entry_default_contact_type_description,
                value = currentSettings.defaultContactType,
                options = contactTypeOptions,
                onValueChanged = { settingsRepository.defaultContactType = it }
            )
        }
    }
}

@Composable
private fun MiscellaneousCategory(settingsRepository: SettingsRepository, currentSettings: ISettingsState) {
    SettingsCategory(titleRes = R.string.settings_category_miscellaneous) {
        SettingsCheckbox(
            label = R.string.settings_entry_error_reports,
            description = R.string.settings_entry_error_reports_description,
            value = currentSettings.sendErrorsToCrashlytics,
            onValueChanged = { settingsRepository.sendErrorsToCrashlytics = it }
        )
    }
}
